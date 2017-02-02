#' Add coverage info.
#'
#' \code{AddCoverageInfo} calculates and adds read coverage to list of sites. This function will 
#' add 5 fields to the initial list: 
#' cov1, cov2: total read coverage per sample,
#' covs1, covs2: read coverage per sample and replicate, and
#' cov: total read coverage (cov1 + cov2). 
#'
#' @param l List object created by \code{Read()}.
#' @return Returns a list of base calls with additional coverage fields.
#'
#' @examples
#' ## add coverage info to data
#' data <- AddCoverageInfo(untr_hek293_rdds)
#' 
#' @export 
AddCoverageInfo <- function(l) {
	if (! is.null(l[["cov"]])) {
		return(l)
	}

	l[["cov1"]] <- Coverage(Samples(l, 1), collapse = TRUE)
	l[["covs1"]]  <- Coverage(Samples(l, 1), collapse = FALSE)

	l[["cov2"]] <- Coverage(Samples(l, 2), collapse = TRUE)
	l[["covs2"]]  <- Coverage(Samples(l, 2), collapse = FALSE)

	l[["cov"]] <- l[["cov1"]] + l[["cov2"]]
	l
}

#' Calculates a base vector for each sample of JACUSA output.
#'
#' \code{AddBaseInfo} calculates a base vector for each sample of JACUSA output and adds the 
#' result to the initial list object.  
#'
#' @param l List object created by \code{Read()}.
#' @param collapse Logical indicates if read counts of sample 2 should be merged - replicates are collapsed. 
#' @return Returns a list of base calls with additional base1 and base2 fields.
#'
#' @examples
#' data <- AddBaseInfo(untr_hek293_rdds)
#' ## plot distribution of bases in sample1
#' barplot(table(data$base1))
#' 
#' @export 
AddBaseInfo <- function(l, collapse = FALSE) {
	if (! is.null(l[["base1"]]) & ! is.null(l[["base2"]])) {
		return(l)
	}
	if (is.null(l[["matrix1"]]))  {
		sample1 <- Samples(l, 1)
		l[["matrix1"]] <- ToMatrix(sample1, collapse = collapse)
	}
	l[["base1"]] <- ToBase(l[["matrix1"]])

	if (is.null(l[["matrix2"]])) {
		sample2 <- Samples(l, 2)
		l[["matrix2"]] <- ToMatrix(sample2, collapse = collapse)
	}
	l[["base2"]] <- ToBase(l[["matrix2"]])
	l
}

#' Calculates base change for RDD comparisons of JACUSA output.
#'
#' \code{AddBaseChangeInfo} calculates base change for gDNA vs. cDNA comparisons and adds the 
#' result to the initial list object.
#'
#'
#' @param l List object created by \code{Read()}.
#' @return Returns a list of base calls with the additional baseChange fields.
#'
#' @examples
#' data <- AddBaseChangeInfo(untr_hek293_rdds)
#' ## plot distribution of base changes
#' barplot(Table(data))
#' 
#' @export 
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
#' \code{AddEditingFreqInfo} calculates the editing frequency for each replicate and an average 
#' for gDNA vs. cDNA comparisons. The result is added to the initial list object.
#' 
#' @param l List object created by \code{Read()}.
#' @return Returns a list of base calls with the additional editingFreq fields.
#'
#' @examples
#' ## AddEditingFreqInfo implicitly adds the baseChange field
#' data <- AddEditingFreqInfo(untr_hek293_rdds)
#' ## plot a boxplot of editing frequencies for each base change
#' boxplot(tapply(data$editingFreq, data$baseChange, c))
#' 
#' @export 
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
  v <- mapply(function(x, y) { gsub(x, "", y) }, b1, b2, USE.NAMES = FALSE)
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
