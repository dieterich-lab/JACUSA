#' Filters sites by read coverage.
#'
#' \code{FilterByCoverage} filters sites by customizable read coverage restrictions.   
#'
#' @param l List object created by \code{Read()}.
#' @param fields Character vector indicates if filtering should be carried out on total read coverage 
#'        of both samples fields = c("cov1", "cov2") or on each replicate of sample 2: fields = c("covs2"). 
#'        Possible values are: "cov1", "cov2", "covs1", or "covs2".
#' @param cov Vector or numeric value of the minimal read coverage.
#' @return Returns List of sites filtered by minimal read coverage according to fields and cov.
#'
#' @examples
#' ## Keep sites that have a total read depth of at least 10 reads in sample 1 and 
#' ## each of sample 2 replicates has a minimal read coverage of 5 reads. 
#' data <- FilterByCoverage(untr_hek293_rdds, fields = c("cov1", "covs2"), cov = c(10, 5))
#' 
#' @export 
FilterByCoverage <- function(l, fields, cov) {
	l <- AddCoverageInfo(l)

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
}, fields, cov, SIMPLIFY = FALSE, USE.NAMES = FALSE)
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
#' @return todo
#'
#' @export
FilterByCoverageHelper <- function(covs, cov) {
	if (is.list(covs)) {
		df <- do.call(cbind, covs)
		apply(df, 1, function(r) { all(r >= cov) })
	} else {
		unlist(
					lapply(covs, function(r) { r >= cov })
					, use.names = FALSE
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
#' ## Filters sites that have less than 2 variant reads in the cDNA sample. 
#' data <- FilterByMinVariantCount(untr_hek293_rdds, min_count = 2)
#' 
#' @export
FilterByMinVariantCount <- function(l, min_count = 2, collapse = FALSE) {
	l <- AddBaseChangeInfo(l)

	if (is.null(l[["matrix2"]])) {
		l[["matrix2"]] <- ToMatrix(Samples(l, 2), collapse = collapse)
	}

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

#' Helper function
#'
#' \code{GetFilterResult}
#'
#' @param l todo
#'
#' @return todo
#'
#' @export
GetFilterResult <- function(l) {
	l <- AddBaseInfo(l, collapse = TRUE)

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
#' result <- FilterResult(untr_hek293_rdds)
#' 
#' @export 
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
#'
#' @return Returns List with sites that are contained in vector f.
#'
#' @examples
#' ## calculate variant count
#' variant_count <- GetVariantCount(untr_hek293_rdds, collapse = TRUE)
#' ## create index of sites that contain at least 10 variant bases
#' index <- variant_count >= 10
#' ## filter data according to index
#' filtered <- FilterRec(untr_hek293_rdds, index)
#' 
#' @export 
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
#' ## filter by test-statistic = 1.56
#' filtered_data <- FilterByStat(untr_hek293_rdds, 1.56)
#' 
#' @export 
FilterByStat <- function(l, stat) {
	i <- l$stat >= stat
	FilterRec(l, i)
}
