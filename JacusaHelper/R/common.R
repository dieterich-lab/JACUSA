#' JacusaHelper: A package for post-processing JACUSA result files.
#'
#' The JacusaHelper package provides three categories of important functions:
#' InputOutput, AddInfo, and Filter.
#' 
#' When calling RDDs where the RNA-Seq sample has been generated with stranded sequencing library, base change(s) can be directly inferred and if necessary base calls can be inverted.
#' Per default DNA need to be provided as sample 1 and cDNA as sample 2!
#' Warning: Some function do not support replicates or are exclusively applicable on RDD or RRD result files!
#' use l <- Read(l) to read the data
#' and then sample <- Samples(l, 1) to extract the sample specific data
#' then continue with ToMatrix(sample)
#' 
#' @section InputOutput functions:
#' The functions Read, and Write facilitate input and output operations on JACUSA output files.
#'
#' @section AddInfo functions:
#' This functions calculate and add additional information such as read depth or base changes. 
#' This includes functions convert base counts that are encoded as character vectors to base count matrices.
#' See: 
#' * AddCoverageInfo
#' * AddBaseChangeInfo
#' * AddEditingFreqInfo
#'
#' @section Filter functions:
#' This function set enables processing of JACUSA output files such as filtering by read coverage or enforcing a minimal number of variant base calls per sample.
#' See:
#' * FilterByStat
#' * FilterResult
#' * FilterByMinVariant
#'
#' @docType package
#' @name JacusaHelper
NULL

# convenience: All possible bases
.BASES <- c("A", "C", "G", "T")

#' Converts base counts from sample columns to matrices.
#' 
#' \code{ToMatrix} converts base counts encoded as "," separated string vectors to count matrices.
#' 
#' @param sample List of string vectors where base counts (A,C,G,T) are separated by ",". Typically, the result of \code{Samples(data, i)}, where i = 1 or 2.
#' @param invert Logical indicates if base counts should be inverted (A=>T, C=>G, etc...) after conversion. Depends on employed sequencing library. 
#' @param collapse Logical indicates if sample is a list of string vectors and if converted matrices should be aggregated - collapses replicates. 
#' @return Returns a matrix or a list of matrices of base counts.
#'
#' @examples
#' ## Read JACUSA result file JACUSA.out asdf
#' data <- Read("JACUSA.out")
#' ## Extract sequencing info of sample 1
#' sample1 <- Samples(data, 1)
#' ## Convert character encoded base counts to count matrices
#' matrix1 <- ToMatrix(sample1)
#' 
#' @export 
ToMatrix <- function(sample, invert = F, collapse = T) {
  # merge matrices sample
  if (is.list(sample))  {
    m <- lapply(sample, .ToMatrixHelper, invert)
    if (collapse) {
      m <- Reduce('+', m)
    }
    m
  } else {
    .ToMatrixHelper(sample, invert)
  }
}

# this helper function will convert one base column vector e.g.: [A, C, G, T] = 10,0,0,0 
# to a matrix and invert base calls when desired
.ToMatrixHelper <- function(sample, invert = F) { 
  l <- strsplit(sample, ",")
  m <- do.call(rbind, l)
  class(m) <- "numeric"
  colnames(m) <- .BASES
  if (invert) { 
    m <- Invert(m) 
  }
  m
}

#' Helper function
#'
#' Calculate read coverage
#'
#' @param sample todo
#' @param collapse todo
#' @return
#'
#' @export
Coverage <- function(sample, collapse = F) {
  m <- ToMatrix(sample, collapse = collapse)
  if (is.list(m)) {
    lapply(m, rowSums)
  } else {
    rowSums(m)
  }
}

#' Filters sites by read coverage.
#'
#' \code{FilterByCoverage} filters sites by customizable read coverage restrictions.   
#'
#' @param l List object created by \code{Read()}.
#' @param fields Character vector indicates if filtering should be carried out on total read coverage of both samples fields = c("cov1", "cov2") or on each replicate of sample 2: fields = c("covs2"). Possible values are: "cov1", "cov2", "covs1", or "covs2".
#' @param cov Vector or numeric value of the minimal read coverage.
#' @return Returns List of sites filtered by minimal read coverage according to fields and cov.
#'
#' @examples
#' ## Read JACUSA result file JACUSA.out
#' data <- Read(data)
#' ## Keep sites that have a total read depth of at least 10 reads in sample 1 and each of sample 2 replicates has a minimal read coverage of 5 reads. 
#' data <- FilterByCoverage(data, fields = c("cov1", "covs2"), cov = c(10, 5))
FilterByCoverage <- function(data, fields, cov) {
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

#' Helper function
#'
#' FilterByCoverage Helper
#'
#' @param covs todo
#' @param cov todo
#' @return
#'
#' @export
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

#' Filters JACUSA results of RDD calls and ensures minimal number of variant reads.
#'
#' \code{FilterByMinVariant} Filters JACUSA result files of gDNA vs. cDNA comparisons and enforces a minimal number of variant bases in cDNA.
#' Per default gDNA is expected to be sample 1 and cDNA sample2!
#'
#' @param l List object created by \code{Read()}.
#' @param min_count Numeric value that specifies the minimal number of variant reads in sample 2 or cDNA.
#' @param collapse Logical indicates if read counts of sample 2 should be merged - replicates are collapsed. 
#' @return Returns List of sites that have at least min_count variant reads in cDNA.  
#'
#' @examples
#' ## Read JACUSA result file JACUSA.out
#' data <- Read(data)
#' ## Filters sites that have less than 2 variant reads in the cDNA sample. 
#' data <- FilterByMinVariantCount(data, min_count = 2)
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
  i <- match(v, .BASES)
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

# FIMXE duplicated code in upper function
#' Calculates the number of variant reads in RDD JACUSA results files
#'
#' Calculates the number of variant reads in JACUSA results of RDD calls
#'
#' Calculates the number of variant reads in JACUSA result files of gDNA vs. cDNA comparisons.
#' Per default gDNA is expected to be sample 1 and cDNA to be sample 2!
#'
#' @param l List object created by \code{Read()}.
#' @param collapse Logical indicates if read counts of sample 2 should be merged - replicates are collapsed. 
#' @return Returns List of count that represent the number of variant reads in cDNA.  
#'
#' @examples
#' ## Read JACUSA result file JACUSA.out
#' data <- Read(data)
#' variant_count <- GetVariantCount(data)
GetVariantCount <- function(l, collapse = F) {
  # reference base
  b1 <- l[["base1"]]
  # cDNA base
  b2 <- l[["base2"]]
  # variant base
  v <- mapply(function(x, y) { gsub(x, "", y) }, b1, b2, USE.NAMES = F)

  # base counts from cDNA
  m2 <- l[["matrix2"]]
  i <- match(v, .BASES)
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
#' Helper function
#'
#' \code{GetFilterResult}
#'
#' @param l todo
#' @return
#'
#' @export
GetFilterResult <- function(l) {
  GetMask <- function(m, op = "&") {
    AnyBases <- function(m) {
      t(apply(m, 1, function(x) {
	      x > 0
		}))
    }
    if (is.list(m)) {
      m <- lapply(m, AnyBases)
      m <- Reduce(op, m)
    } else {
      m <- AnyBases(m)
    }
    m
  }

  cm <- list()
  if (! is.list(l$matrix1)) {
    cm <- list(bases11 = l$matrix1)
  } else {
    cm <- l$matrix1
  }
  if (! is.list(l$matrix2)) {
    cm$bases21 <- l$matrix2
  } else {
    cm <- c(cm, l$matrix2)
  }

  m <- GetMask(cm, op = "|")
  m1 <- GetMask(l$matrix1)
  m2 <- GetMask(l$matrix2)

  b <- (m1 | m2) == m
  i <- apply(b, 1, function(x) {
	     all(x)
    })
  i
}

#' Retains sites that contain the variant base in all replicates of at least one sample.
#'
#' \code{FilterResults} Enforces that at least one sample contains the variant base in all replicates. 
#'
#' @param l List object created by \code{Read()}.
#' @return Returns List with sites where at least one sample contains the variant base in all replicates.  
#'
#' @examples
#' ## Read JACUSA result file JACUSA.out
#' data <- Read(data)
#' result <- FilterResult(data)
FilterResult <- function(l) {
  i <- GetFilterResult(l)
  FilterRec(l, i)
}

#' Filters List of sites recursively.
#'
#' \code{FilterRec} filters list of sites recursively.
#'
#' @param l List object created by \code{Read()}.
#' @param f Vector of sites that should be retained.
#' @return Returns List with sites that are contained in vector f.
#'
#' @examples
#' ## Read JACUSA result file JACUSA.out - RDD calls
#' data <- Read(data)
#' ## calculate variant count
#' variant_count <- GetVariantCount(data, collapse = T)
#' ## create index of sites that contain at least 10 variant bases
#' index <- variant_count >=10
#' ## filter data according to index
#' filtered_data <- FilterResult(data, index)
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

#' Filters List of sites by test-statistic.
#'
#' \code{FilterByStat} removes sites that as less than some threshold that has been provided by user.
#'
#' @param l List object created by \code{Read()}.
#' @param stat Numeric value that represents the minimal test-statistic.
#' @return Returns List with sites with a test-statistic >= stat.
#'
#' @examples
#' ## Read JACUSA result file JACUSA.out
#' data <- Read(data)
#' ## filter by test-statistic = 1.56
#' filtered_data <- FilterByStat(data, 1.56)
FilterByStat <- function(l, stat) {
  i <- l$stat >= stat
  FilterRec(l, i)
}

#' Helper function
#'
#' \codeToString} convert from count matrix to vector of character
#'
#' @param m todo
#' @return
#'
#' @export
ToString <- function(m) {
  if (is.list(m)) {
    lapply(m, .ToStringHelper)
  } else {
    .ToStringHelper(m) 
  }
}
.ToStringHelper <- function(m) {
  apply(m, 1, paste, collapse = ",")
}

#' Helper function
#'
#' returns the observed bases per site as a string
#'
#' @param d todo
#' @return
#'
#' @export
ToBase <- function(d) {
  if (is.list(d)) {
    d <- Reduce('+', d)
  }

  apply(d, 1, function(x) { b <- names(x)[x > 0] ; paste(b, collapse = "") } ) 
}

#' Helper function
#'
#' returns the observed bases per site as a vector of characters
#'
#' @param d todo
#' @return
#'
#' @export
ToBases <- function(d) {
  if (is.list(d)) {
    d <- Reduce('+', d)
  }
  apply(d, 1, function(x) { b <- names(x)[x > 0] ; return(b) } )
}

#' Returns base call columns for a sample from a JACUSA result file.
#'
#' \code{Samples} returns base call columns for a sample (1 or 2) from a JACUSA result file.
#'
#' @param l List object created by \code{Read()}.
#' @param sample Integer value: 1 or 2.
#' @return Returns a list of base calls "," separated for choosen sample.
#'
#' @examples
#' ## Read JACUSA result file JACUSA.out
#' data <- Read(data)
#' ## Extract sequencing information for sample 1. 
#' sample1 <- Samples(data, 1)
Samples <- function(l, sample) {
  sample <- paste("bases", sample, sep = "")
  j <- grep(sample, names(l))
  if (length(j) > 1) {
    l[j]
  } else {
    l[[j]]
  }
}

#' Reads JACUSA output.
#'
#' \code{Read} reads JACUSA output and returns a list of identified sites. See JACUSA manual for details on encoding of replicates and samples.
#'
#' @param f String represents the filename of the JACUSA output. 
#' @param invert Logical indicates if base calls should be inverted.
#' @param stat Numeric value represents the minimal test-statistic.
#' @param fields Vector of strings defines how to filter by read coverage
#' @param cov Vector of numeric defines the minimal read coverage.
#' @param collapse Logical indicates if replicates should be collapsed - replicates of base count vectors will be aggregated.
#' @return Returns a list of base calls "," separated for chosen parameters.
#'
#' @examples
#' ## Read JACUSA result file JACUSA.out, invert base count and retain sites with test-statistic >= 1.56
#' data <- Read(data, invert = T, stat = 1.56)
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

#' Add coverage info.
#'
#' \code{AddCoverageInfo} calculates and adds read coverage to list of sites. This function will add 5 fields to the initial list: 
#' cov1, cov2: total read coverage per sample,
#' covs1, covs2: read coverage per sample and replicate, and
#' cov: total read coverage (cov1 + cov2). 
#'
#' @param l List object created by \code{Read()}.
#' @return Returns a list of base calls with additional coverage fields.
#'
#' @examples
#' ## Read JACUSA result file JACUSA.out
#' data <- Read(data)
#' ## add coverage info to data
#' data <- AddCoverageInfo(data)
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

#' Calculates a base vector for each sample of JACUSA output.
#'
#' \code{AddBaseInfo} calculates a base vector for each sample of JACUSA output and adds the result to the initial list object.  
#'
#' @param l List object created by \code{Read()}.
#' @return Returns a list of base calls with additional base1 and base2 fields.
#'
#' @examples
#' ## Read JACUSA result file JACUSA.out
#' data <- Read(data)
#' data <- AddBaseInfo(data)
#' ## plot distribution of bases in sample1
#' barplot(table(data$base1))
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

#' Calculates base change for RDD comparisons of JACUSA output.
#'
#' \code{AddBaseChangeInfo} calculates base change for gDNA vs. cDNA comparisons and adds the result to the initial list object.  
#'
#' @param l List object created by \code{Read()}.
#' @return Returns a list of base calls with the additional baseChange fields.
#'
#' @examples
#' ## Read JACUSA result file JACUSA.out
#' data <- Read(data)
#' data <- AddBaseChangeInfo(data)
#' ## plot distribution of base changes
#' barplot(Table(data))
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

#' Calculates editing frequency for RDDs in JACUSA output.
#'
#' \code{AddEditingFreqInfo} calculates the editing frequency for each replicate and an average for gDNA vs. cDNA comparisons. The result is added to the initial list object.
#' 
#' @param l List object created by \code{Read()}.
#' @return Returns a list of base calls with the additional editingFreq fields.
#'
#' @examples
#' ## Read JACUSA result file JACUSA.out
#' data <- Read(data)
#' ## AddEditingFreqInfo implicitly adds the baseChange field
#' data <- AddEditingFreqInfo(data)
#' ## plot a boxplot of editing frequencies for each base change
#' boxplot(tapply(data$editingFreq, data$baseChange, c))
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
  i <- match(v, .BASES)

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

#' Calculates the distribution of base changes of RDDs in JACUSA output.
#'
#' \code{Calculates} the distribution of base changes of RDDs in JACUSA output.
#' 
#' @param l List object created by \code{Read()}.
#' @param fixAlleles Logical indicates if list of sites should be filtered to ensure that each site contains a maximum of 2 alleles.
#' @return Returns a vector of numeric values that contains the number of observed base changes.
#'
#' @examples
#' ## Read JACUSA result file JACUSA.out
#' data <- Read(data)
#' ## Table implicitly populates the baseChange field
#' tbl <- Table(data)
#' plot(tbl)
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

#' Calculates the fraction of editing sites among RDDs in JACUSA output.
#'
#' \code{Score} calculates the fraction of editing sites among RDDs in JACUSA output.
#' 
#' @param tbl Vector object created by \code{Table()}.
#' @param editing Vector of strings that identifies true editing, e.g.: "A->G".
#' @return Returns a numeric values that represent the fraction of editing sites among RDDs.
#'
#' @examples
#' ## Read JACUSA result file JACUSA.out
#' data <- Read(data)
#' ## Table implicitly populates the baseChange field
#' tbl <- Table(data)
#' Score(tbl)
Score <- function(tbl, editing = c("A->G")) {
  total <- sum(tbl)
  TP <- sum(tbl[editing])
  return(TP / total)
}

#' Helper function
#'
#' invert the base calls of a base call matrix
#'
#' @param m todo
#' @return
#'
#' @export
Invert <- function(m) {
  tmp <- m 
  tmp[, "A"] <- m[, "T"]
  tmp[, "C"] <- m[, "G"]
  tmp[, "G"] <- m[, "C"]
  tmp[, "T"] <- m[, "A"]
  return(tmp)
}

#' Helper function
#'
#' invert vector of base calls
#'
#' @param m todo
#' @return
#'
#' @export
InvertBase <- function(b) {
  r <- rep("", length(b))
  mapply(function(o, c) {
    i <- unlist(lapply(b, function(x) { any(x %in% o) } ))
    r[i] <<- paste(r[i], c, sep = "")
  }, .BASES, rev(.BASES))
  r
}
#' Helper function
#'
#' assuming some true editing gives the fraction of false editing
#'
#' @param l todo
#' @param editing todo
#' @return
#'
#' @export
FalsePositives <- function(l, editing) {
  FP <- 1 - Score(l, editing)
  FP
}

#' Writes a list of sites to a file.
#'
#' \code{Write} Stores a list of sites in a file.
#' 
#' @param l Vector object created by \code{Table()}.
#' @param file String is the filename to store the list object.
#' @param extra Vector of strings that defines additional elements from the list that will be stored in the file. 
#'
#' @examples
#' ## Read JACUSA result file JACUSA.out
#' data <- Read(data)
#' data <- AddBaseChangeInfo(data)
#' ## base change will be stored in the id/name column according to the BED file format definition.
#' data$name <- data$baseChange
#' Write(data)
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

#' Helper function
#'
#' formats editing of two base call vectors
#'
#' @param a todo
#' @param b todo
#' @return
#'
#' @export
Editing <- function(a, b) {
  paste(a, sep = "->", b)
}

#' Helper function
#'
#' plots the distribution of base changes
#'
#' @param tbl todo
#' @param score todo
#' @return
#'
#' @export
PlotTable <- function(tbl, score = T) {
  main <- ""
  if (score) {
    score <- Score(tbl)
    main <- paste("A->G (", format(score * 100, digits = 4), "%)", sep = "")  
  }
  barplot(tbl, las = 2, main = main, ylab = "Frequency")
}

# Helper function - create VCF 
# FIXME add invert as parameter 
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
# Helper function
# returns the editing frequency of all sites from two gDNA vs. cDNA comparisons: RDDx and RDDy
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
# Helper function
PaperTheme <- function(...) {
  theme(plot.title = element_text(face = "bold", size = 20), # use theme_get() to see available options
        axis.title.x = element_text(face = "bold", size = 16),
        axis.title.y = element_text(face = "bold", size = 16, angle = 90),
        panel.grid.major = element_blank(), # switch off major gridlines
        panel.grid.minor = element_blank(), # switch off minor gridlines 
        ...
        )
}
