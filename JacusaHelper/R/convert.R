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
#' ## Read JACUSA result file hek293_untreated.out asdf
#' data <- Read("hek293_untreated.out")
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
#' \codeToString} convert from count matrix to vector of character
#'
#' @param m todo
#' @return todo
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
#' @return todo
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
#' @return todo
#'
#' @export
ToBases <- function(d) {
	if (is.list(d)) {
		d <- Reduce('+', d)
	}
	apply(d, 1, function(x) { b <- names(x)[x > 0] ; return(b) } )
}
