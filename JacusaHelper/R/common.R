# Author: Michael Piechotta
# Contains scripts to process JACUSA output in R

# Info:
# Note for function 'Samples(l, sample)' : sample in {1, 2} corresponding to the first or second sample (replicates are considered separately)
# Per default DNA is expected to be sample 1
# The varialbe 'invert' indicates if we are dealing with Illumina sequencing data (orientation needs to be inverted)
# Some function support replicates

# use l <- Read(l) to read the data
# and then sample <- Samples(l, 1) to extract the specific sample
# then continue with ToMatrix(sample)

BASES <- c("A", "C", "G", "T")

ToMatrix <- function(sample, invert = F, collapse = T) {
  # merge matrices sample1
  if (is.list(sample))  {
    m <- lapply(sample, ToMatrixHelper, invert)
    if (collapse) {
      m <- Reduce('+', m)
    }
    m
  } else {
    ToMatrixHelper(sample, invert)
  }
}
ToMatrixHelper <- function(sample, invert = F) { 
  l <- strsplit(sample, ",")
  m <- do.call(rbind, l)
  class(m) <- "numeric"
  colnames(m) <- c("A", "C", "G", "T")
  if (invert) { 
    m <- Invert(m) 
  }
  m
}

Coverage <- function(sample, collapse = F) {
  m <- ToMatrix(sample, collapse = collapse)
  if (is.list(m)) {
    lapply(m, rowSums)
  } else {
    rowSums(m)
  }
}
# possible fields cov1, cov2, all
FilterByCoverage <- function(l, fields, cov) {
  AddCoverageInfo(l)

  i <- mapply(function(f, c) {
              if (is.list(l[[f]])) {
                j <- lapply(l[[f]], function(x) {
                            x >= c
                })
                j <- do.call(cbind, j)
                j <- apply(j, 1, function(x) {
                  all(x)
                })
                j
              } else {
                l[[f]] >= c
              }
  }, fields, cov, SIMPLIFY = F, USE.NAMES = F)
  if (is.list(i)) {
    i <- do.call(cbind, i)
    i <- apply(i, 1, function(x) {
      all(x)
    })
  }

  FilterRec(l, i)
}
FilterByCoverageHelper <- function(covs, cov) {
  if (is.list(covs)) {
    df <- do.call(cbind, covs)
    apply(df, 1, function(r) { all(r >= cov) })
  } else {
    unlist(
           lapply(covs, function(r) { r >= cov })
           , use.names = F
           )
  }
}
# Ensure that in cDNA are at least minCount variant bases
FilterByMinVariantCount <- function(l, min_count = 2, collapse = F) {
  l <- AddBaseChangeInfo(l)

  if (is.null(l[["matrix2"]])) {
    l[["matrix2"]] <- ToMatrix(Samples(l, 2), collapse = collapse)
  }

  # reference base
  b1 <- l[["base1"]]
  # cDNA base
  b2 <- l[["base2"]]
  # variant base
  v <- mapply(function(x, y) { gsub(x, "", y) }, b1, b2, USE.NAMES = F)

  # base counts from cDNA
  m2 <- l[["matrix2"]]
  i <- match(v, BASES)
  f <- c()
  if (is.list(m2)) {
    f <- lapply(m2, function(m) {
                m[cbind(1:nrow(m), i)] < 1
    })
    f <- do.call(cbind, f)
    f <- apply(f, 1, any)
  } else {
    f <- m2[cbind(1:nrow(m2), i)] < min_count
  }
  l <- FilterRec(l, ! f)
  l
}
GetVariantCount <- function(l, collapse = F) {
  # reference base
  b1 <- l[["base1"]]
  # cDNA base
  b2 <- l[["base2"]]
  # variant base
  v <- mapply(function(x, y) { gsub(x, "", y) }, b1, b2, USE.NAMES = F)

  # base counts from cDNA
  m2 <- l[["matrix2"]]
  i <- match(v, BASES)
  f <- c()
  if (is.list(m2)) {
    c <- lapply(m2, function(m) {
                m[cbind(1:nrow(m), i)]
    })
  } else {
    c <- m2[cbind(1:nrow(m2), i)]
  }
  c
}

FilterRec <- function(l, f) {
  lapply(l, function(x) {
    if (is.list(x)) {
      lapply(x, function(e) {
        if (is.matrix(e)) {
          e[f, ]
        } else {
          e[f]
        }
      })
    } else if (is.matrix(x)) {
      x[f, ]
    } else {
      x[f]
    }
  })
}

FilterByStat <- function(l, stat) {
  i <- l$stat >= stat
  FilterRec(l, i)
}

# convert from matrix to string for saving
ToString <- function(m) {
  if (is.list(m)) {
    lapply(m, ToStringHelper)
  } else {
    ToStringHelper(m) 
  }
}
ToStringHelper <- function(m) {
  apply(m, 1, paste, collapse = ",")
}

ToBase <- function(d) {
  if (is.list(d)) {
    d <- do.call('+', d)
  }

  apply(d, 1, function(x) { b <- names(x)[x > 0] ; paste(b, collapse = "") } ) 
}
ToBases <- function(d) {
  if (is.list(d)) {
    d <- do.call('+', d)
  }
  apply(d, 1, function(x) { b <- names(x)[x > 0] ; return(b) } )
}

Samples <- function(l, sample) {
  sample <- paste("bases", sample, sep = "")
  j <- grep(sample, names(l))
  if (length(j) > 1) {
    l[j]
  } else {
    l[[j]]
  }
}

Read <- function(f, invert = F, stat = NULL, header = T, fields = NULL, cov = NULL, collapse = F, ...) {
  d <- read.table(f, header = header, stringsAsFactors = F, check.names = F, comment.char = "", ...) 
  colnames(d)[1] <- gsub("^#", "", colnames(d)[1])
  l <- as.list(d)

  if (! is.null(stat)) {
    l <- FilterByStat(l, stat)
  }

  if (! is.null(cov) & ! is.null(fields)) {
    l <- AddCoverageInfo(l)
    l <- FilterByCoverage(l, cov = cov, fields = fields)
  }

  if (! is.null(invert)) {
    if(invert) {
      m1 <- ToMatrix(Samples(l, 1), invert = T, collapse = F)
      i <- grep("bases1", names(l))
      if (length(i) == 1) {
        l[[i]] <- ToString(m1)
      } else {
        l[i] <- ToString(m1)
      }
      m2 <- ToMatrix(Samples(l, 2), invert = T, collapse = F)
      i <- grep("bases2", names(l))
      if (length(i) == 1) {
        l[[i]] <- ToString(m2)
      } else {
        l[i] <- ToString(m2)
      }

      strand <- l[["strand"]]
      l[["strand"]][strand == "+"] <- "-"
      l[["strand"]][strand == "-"] <- "+"
    }
  }
  l
}
AddCoverageInfo <- function(l) {
  if (! is.null(l[["cov"]])) {
    return(l)
  }

  l[["cov1"]] <- Coverage(Samples(l, 1), collapse = T)
  l[["covs1"]]  <- Coverage(Samples(l, 1), collapse = F)

  l[["cov2"]] <- Coverage(Samples(l, 2), collapse = T)
  l[["covs2"]]  <- Coverage(Samples(l, 2), collapse = F)

  l[["cov"]] <- l[["cov1"]] + l[["cov2"]]
  l
}

AddBaseInfo <- function(l) {
  if (! is.null(l[["base1"]]) & ! is.null(l[["base2"]])) {
    return(l)
  }
  if (is.null(l[["matrix1"]]))  {
    sample1 <- Samples(l, 1)
    l[["matrix1"]] <- ToMatrix(sample1, collapse = F)
  }
  l[["base1"]] <- ToBase(l[["matrix1"]])

  if (is.null(l[["matrix2"]])) {
    sample2 <- Samples(l, 2)
    l[["matrix2"]] <- ToMatrix(sample2, collapse = F)
  }
  l[["base2"]] <- ToBase(l[["matrix2"]])
  l
}

AddBaseChangeInfo <- function(l) {
  if (! is.null(l[["baseChange"]])) {
    return(l)
  }
  l <- AddBaseInfo(l)
  
  b1 <- l[["base1"]]
  b2 <- l[["base2"]]
  b2 <- mapply(function(dna, rna) {
    gsub(dna, "", rna)
  }, b1, b2)
  l[["baseChange"]] <- paste(b1, b2, sep = "->")
  l[["baseChange"]][b2 == ""] <- "no change"
  l
}

AddEditingFreqInfo <- function(l) {
  if (! is.null(l[["editingFreq"]])) {
    return(l)
  }
  l <- AddBaseChangeInfo(l)

  # reference base
  b1 <- l[["base1"]]
  # cDNA base
  b2 <- l[["base2"]]
  # variant base
  v <- mapply(function(x, y) { gsub(x, "", y) }, b1, b2, USE.NAMES = F)
  # base counts
  m1 <- l[["matrix1"]]
  m2 <- l[["matrix2"]]
  i <- match(v, BASES)

  freq <- c()
  if (is.list(m2)) {
    freq <- lapply(m2, function(m) {
      f <- m[cbind(1:nrow(m), i)] / rowSums(m)
      f[is.na(f)] <- 0.0
      f
    })
    for (j in c(1:length(m2))) {
      l[[paste0("editingFreq", j)]] <- freq[[j]]
    }
    freq <- do.call(cbind, freq)
    freq <- rowMeans(freq)
  } else {
    freq <- m2[cbind(1:nrow(m2), i)] / rowSums(m2)
    freq[is.na(freq)] <- 0.0
  }

  l[["editingFreq"]] <- freq
  l
}

# collapse replicates and give conversion table
Table <- function(l, fixAlleles = F) {
  l <- AddBaseChangeInfo(l)

  base_change <- l[["baseChange"]]
  tbl <- table(base_change)

  if (! fixAlleles) { return(tbl) }
  # remove sites with more than 2 alleles

  b1 <- ToBases(m1)
  b2 <- ToBases(m2)

  # get index of sites with more than two alleles
  i <- mapply(function(a, b) {
    ! length(unique(c(a, b))) > 2
  }, bs1, bs2)

  return(tbl(baseChange[i]))
}

Score <- function(tbl, editing = c("A->G")) {
  total <- sum(tbl)
  TP <- sum(tbl[editing])
  return(TP / total)
}

Invert <- function(m) {
  tmp <- m 
  tmp[, "A"] <- m[, "T"]
  tmp[, "C"] <- m[, "G"]
  tmp[, "G"] <- m[, "C"]
  tmp[, "T"] <- m[, "A"]
  return(tmp)
}
InvertBase <- function(b) {
  r <- rep("", length(b))
  bases <- c("A", "C", "G", "T")
  mapply(function(o, c) {
    i <- unlist(lapply(b, function(x) { any(x %in% o) } ))
    r[i] <<- paste(r[i], c, sep = "")
  }, bases, rev(bases))
  r
}

FalsePositives <- function(l, editing) {
  FP <- 1 - Score(l, editing)
  FP
}

Write <- function(l, file, extra = NULL) {
  fields <- c("contig", "start", "end", "name", "stat", "strand", "info", "filter_info")
  if (! is.null(extra)) {
    fields <- c(fields, extra)
  }
  fields <- names(l)[names(l) %in% fields]
  i <- grep("^bases", names(l))
  fields <- c(fields, names(l)[i])
  l <- l[fields]
  d <- as.data.frame(l, stringsAsFactors = F, check.names = F)
  colnames(d)[1] <- paste0("#", colnames(d)[1])
  write.table(d, file, col.names = T, row.names = F, quote = F, sep = "\t")
}

Editing <- function(a, b) {
  paste(a, sep = "->", b)
}

PlotTable <- function(tbl, score = T) {
  main <- ""
  if (score) {
    score <- Score(tbl)
    main <- paste("A->G (", format(score * 100, digits = 4), "%)", sep = "")  
  }
  barplot(tbl, las = 2, main = main, ylab = "Frequency")
}

BED2VCF <- function(l) {
  chrom <- l[["contig"]]
  pos <- l[["end"]]
  strand <- l[["strand"]]

  l <- AddBaseInfo(l)

  ref <- l$base1
  alt <- l$base2

  alt <- mapply(function(x, y) { gsub(x, "", y) }, ref, alt, USE.NAMES = F)

  # invert bases
  i <- strand == "-"
  if (length(which(i)) > 0) {
    ref[i] <- InvertBase(ref[i])
    alt[i] <- InvertBase(alt[i])
  }
  n <- length(l$name)

  return(data.frame("#CHROM" = chrom, POS = pos, ID = rep(".", n), REF = ref, ALT = alt, QUAL = rep(".", n), FILTER = rep(".", n), check.names = F))
}

GetEditingFreq <- function(RDDx, RDDy, all = F) {
  RDDx <- AddCoverageInfo(RDDx)
  RDDx <- AddEditingFreqInfo(RDDx)
  RDDx$coord <- paste(RDDx$contig, RDDx$start, RDDx$end, RDDx$end, RDDx$strand, sep = "|")
  
  RDDy <- AddCoverageInfo(RDDy)
  RDDy <- AddEditingFreqInfo(RDDy)
  RDDy$coord <- paste(RDDy$contig, RDDy$start, RDDy$end, RDDy$end, RDDy$strand, sep = "|")

  data <- merge(
                as.data.frame(
                              RDDx[c("coord", "cov2", "baseChange", "editingFreq")], stringsAsFactors = F, check.names = F
                ),
                as.data.frame(
                              RDDy[c("coord", "cov2", "baseChange", "editingFreq")], stringsAsFactors = F, check.names = F
                ),
                by = "coord",
                suffixes = c("_x", "_y"),
                all = all
                )
 
  repx <- data[["editingFreq_x"]]
  repx[is.na(repx)] <- 0
  covx <- data[["cov2_x"]]
  covx[is.na(covx)] <- 0
  base_change_x <- data[["baseChange_x"]]

  repy <- data[["editingFreq_y"]]
  repy[is.na(repy)] <- 0
  covy <- data[["cov2_y"]]
  covy[is.na(covy)] <- 0
  base_change_y <- data[["baseChange_y"]]

  base_change <- cbind(data[["baseChange_x"]], data[["baseChange_y"]])
  base_change <- apply(base_change, 1, function(x) {
                       if (any(is.na(x))) {
                         x[! is.na(x)]
                       } else if(any(x %in% "no change")) {
                         i <- x %in% "no change"
                         if (all(i)) {
                           "no change"
                         } else {
                           x[! i]
                         }
                       } else if (x[1] != x[2]) {
                         "multiple"
                       } else {
                         x[1]
                       }
                })

  df <- data.frame(
                   editingFreq_x = repx, 
                   cov_x = covx, 
                   editingFreq_y = repy, 
                   cov_y = covy,
                   baseChange = base_change,
                   baseChange_x = base_change_x, 
                   baseChange_y = base_change_y, 
                   stringsAsFactors = F, check.names = F)
}

PaperTheme <- function(...) {
  theme(plot.title = element_text(face = "bold", size = 20), # use theme_get() to see available options
        axis.title.x = element_text(face = "bold", size = 16),
        axis.title.y = element_text(face = "bold", size = 16, angle = 90),
        panel.grid.major = element_blank(), # switch off major gridlines
        panel.grid.minor = element_blank(), # switch off minor gridlines 
        ...
        )
}
