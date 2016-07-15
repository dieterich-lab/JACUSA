#' Reads JACUSA output.
#'
#' \code{Read} reads JACUSA output and returns a list of identified sites. See JACUSA manual for 
#' details on encoding of replicates and samples.
#'
#' @param f String represents the filename of the JACUSA output. 
#' @param invert Logical indicates if base calls should be inverted.
#' @param stat Numeric value represents the minimal test-statistic.
#' @param fields Vector of strings defines how to filter by read coverage
#' @param cov Vector of numeric defines the minimal read coverage.
#' @param collapse Logical indicates if replicates should be collapsed - replicates of base 
#'        count vectors will be aggregated.
#' @param ... Additional parameters that will be forwarded to read.table.
#' @return Returns a list of base calls "," separated for chosen parameters.
#'
#' @examples
#' ## Read JACUSA result file hek293_untreated.out, invert base count and 
#' ## retain sites with test-statistic >= 1.56
#' data <- Read(data, invert = T, stat = 1.56)
#' 
#' @export 
Read <- function(f, invert = F, stat = NULL, fields = NULL, cov = NULL, collapse = F, ...) {
	print(getwd())
	d <- read.table(f, header = T, stringsAsFactors = F, check.names = F, comment.char = "", ...) 
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

#' Writes a list of sites to a file.
#'
#' \code{Write} Stores a list of sites in a file.
#' 
#' @param l Vector object created by \code{Table()}.
#' @param file String is the filename to store the list object.
#' @param extra Vector of strings that defines additional elements from the list that 
#'        will be stored in the file. 
#'
#' @examples
#' ## Read JACUSA result file hek293_untreated.out
#' data <- Read("hek293_untreated.out")
#' data <- AddBaseChangeInfo(data)
#' ## base change will be stored in the id/name column according to the BED file format definition.
#' data$name <- data$baseChange
#' Write(data, "JACUSA_modified.out")
#' 
#' @export 
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
