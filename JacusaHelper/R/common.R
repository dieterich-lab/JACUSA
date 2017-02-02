#' JacusaHelper: A package for post-processing JACUSA result files.
#'
#' The JacusaHelper package provides three categories of important functions:
#' InputOutput, AddInfo, and Filter.
#' 
#' When calling RDDs where the RNA-Seq sample has been generated with stranded sequencing library, 
#' base change(s) can be directly inferred and if necessary base calls can be inverted.
#' Per default DNA need to be provided as sample 1 and cDNA as sample 2!
#' Warning: Some function do not support replicates or are exclusively applicable on RDD or RRD 
#' result files!
#' use l <- Read(l) to read the data
#' and then sample <- Samples(l, 1) to extract the sample specific data
#' then continue with ToMatrix(sample)
#' 
#' @section InputOutput functions:
#' The functions Read, and Write facilitate input and output operations on JACUSA output files.
#'
#' See:
#' \itemize{
#'   \item Read
#'   \item Write
#' }
#'
#' @section AddInfo functions:
#' This functions calculate and add additional information such as read depth or base changes. 
#' This includes functions convert base counts that are encoded as character vectors to base count 
#' matrices.
#'
#' See:
#' \itemize{
#'   \item AddCoverageInfo
#'   \item AddBaseInfo
#'   \item AddBaseChangeInfo
#'   \item AddEditingFreqInfo
#' }
#'
#' @section Filter functions:
#' This function set enables processing of JACUSA output files such as filtering by read coverage 
#' or enforcing a minimal number of variant base calls per sample.
#'
#' See:
#' \itemize{
#'   \item FilterByCoverage
#'   \item FilterByStat
#'   \item FilterResult
#'   \item FilterByMinVariantCount
#' }
#'
#' @docType package
#' @name JacusaHelper
NULL

# convenience: All possible bases
.BASES <- c("A", "C", "G", "T")

#' Helper function
#'
#' Calculate read coverage
#'
#' @param sample todo
#' @param collapse todo
#' @return todo
#'
#' @export
Coverage <- function(sample, collapse = FALSE) {
	m <- ToMatrix(sample, collapse = collapse)

	if (is.list(m)) {
		lapply(m, rowSums)
	} else {
		rowSums(m)
	}
}

# FIMXE duplicated code
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
#' @export 
GetVariantCount <- function(l, collapse = FALSE) {
	l <- AddBaseInfo(l, collapse)

	# reference base
	b1 <- l[["base1"]]
	# cDNA base
	b2 <- l[["base2"]]
	# variant base
	v <- mapply(function(x, y) { gsub(x, "", y) }, b1, b2, USE.NAMES = FALSE)

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

#' Returns base call columns for a sample from a JACUSA result file.
#'
#' \code{Samples} returns base call columns for a sample (1 or 2) from a JACUSA result file.
#'
#' @param l List object created by \code{Read()}.
#' @param sample Integer value: 1 or 2.
#'
#' @return Returns a list of base calls "," separated for choosen sample.
#'
#' @examples
#' ## Read JACUSA result file hek293_untreated.out
#' ## Extract sequencing information for sample 1. 
#' sample1 <- Samples(untr_hek293_rdds, 1)
#' 
#' @export 
Samples <- function(l, sample) {
	sample <- paste("bases", sample, sep = "")
	j <- grep(sample, names(l))
	if (length(j) > 1) {
		l[j]
	} else {
		l[[j]]
	}
}

#' Calculates the distribution of base changes of RDDs in JACUSA output.
#'
#' \code{Calculates} the distribution of base changes of RDDs in JACUSA output.
#' 
#' @param l List object created by \code{Read()}.
#' @param fixAlleles Logical indicates if list of sites should be filtered to ensure 
#'        that each site contains a maximum of 2 alleles.
#'
#' @return Returns a vector of numeric values that contains the number of observed base changes.
#'
#' @examples
#' ## Table implicitly populates the baseChange field
#' tbl <- Table(untr_hek293_rdds)
#' plot(tbl)
#' 
#' @export 
Table <- function(l, fixAlleles = FALSE) {
	l <- AddBaseChangeInfo(l)

	baseChange <- l[["baseChange"]]
	tbl <- table(baseChange)

	if (! fixAlleles) { return(tbl) }
	# remove sites with more than 2 alleles

	b1 <- ToBases(l$matrix1)
	b2 <- ToBases(l$matrix2)

	# get index of sites with more than two alleles
	i <- mapply(function(a, b) {
							! length(unique(c(a, b))) > 2
		}, b1, b2)

	return(tbl(baseChange[i]))
}

#' Calculates the fraction of editing sites among RDDs in JACUSA output.
#'
#' \code{Score} calculates the fraction of editing sites among RDDs in JACUSA output.
#' 
#' @param tbl Vector object created by \code{Table()}.
#' @param editing Vector of strings that identifies true editing, e.g.: "A->G".
#'
#' @return Returns a numeric values that represent the fraction of editing sites among RDDs.
#'
#' @examples
#' ## Table implicitly populates the baseChange field
#' tbl <- Table(untr_hek293_rdds)
#' Score(tbl)
#' 
#' @export 
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
#'
#' @return todo
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
#' @param b todo
#'
#' @return todo
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
#'
#' @return todo
#'
#' @export
FalsePositives <- function(l, editing) {
	FP <- 1 - Score(l, editing)
	FP
}

#' Helper function
#'
#' formats editing of two base call vectors
#'
#' @param a todo
#' @param b todo
#' @return todo
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
#'
#' @return todo
#'
#' @export
PlotTable <- function(tbl, score = TRUE) {
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

	alt <- mapply(function(x, y) { gsub(x, "", y) }, ref, alt, USE.NAMES = FALSE)

	# invert bases
	i <- strand == "-"
	if (length(which(i)) > 0) {
		ref[i] <- InvertBase(ref[i])
		alt[i] <- InvertBase(alt[i])
	}
	n <- length(l$name)

	return(data.frame("#CHROM" = chrom, POS = pos, ID = rep(".", n), REF = ref, ALT = alt, QUAL = rep(".", n), FILTER = rep(".", n), check.names = FALSE))
}

# Helper function
# returns the editing frequency of all sites from two gDNA vs. cDNA comparisons: RDDx and RDDy
GetEditingFreq <- function(RDDx, RDDy, all = FALSE) {
	RDDx <- AddCoverageInfo(RDDx)
	RDDx <- AddEditingFreqInfo(RDDx)
	RDDx$coord <- paste(RDDx$contig, RDDx$start, RDDx$end, RDDx$end, RDDx$strand, sep = "|")

	RDDy <- AddCoverageInfo(RDDy)
	RDDy <- AddEditingFreqInfo(RDDy)
	RDDy$coord <- paste(RDDy$contig, RDDy$start, RDDy$end, RDDy$end, RDDy$strand, sep = "|")

	data <- merge(
								as.data.frame(
															RDDx[c("coord", "cov2", "baseChange", "editingFreq")], stringsAsFactors = FALSE, check.names = FALSE
															),
								as.data.frame(
															RDDy[c("coord", "cov2", "baseChange", "editingFreq")], stringsAsFactors = FALSE, check.names = FALSE
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
									stringsAsFactors = FALSE, check.names = FALSE)
}

# Helper function
PaperTheme <- function(...) {
	# use theme_get() to see available options
	ggplot2::theme(plot.title = ggplot2::element_text(face = "bold", size = 20), 
		axis.title.x = ggplot2::element_text(face = "bold", size = 16),
		axis.title.y = ggplot2::element_text(face = "bold", size = 16, angle = 90),
		panel.grid.major = ggplot2::element_blank(), # switch off major gridlines
		panel.grid.minor = ggplot2::element_blank(), # switch off minor gridlines 
		...
	)
}
