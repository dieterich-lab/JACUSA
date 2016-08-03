JACUSA
======

JAVA framework for accurate SNV assessment

Find source code and tools in the following sub-directories of the repository:

* **src/** The main Java source code for JACUSA
* **manual/manual.pdf** The manual for JACUSA 
* **JacusaHelper** R package to process JACUSA output file(s)
* **tools/AddVariants** Java tool to implant variants into BAM file

Requirements
------------

JACUSA has been developed and tested with Java v1.7

Download
--------

Get the current Jacusa JAR:

```
$ https://github.com/dieterich-lab/JACUSA/blob/master/build/JACUSA_v1.0.0.jar
```

Usage
-----

Available methods for JACUSA ```$ java -jar jacusa.jar [ENTER]```: 

* call-2	Call variants - two samples
* pileup	SAMtools like mpileup for two samples

General command line structure for variant calling *call-2*:

```
jacusa.jar call-2 [OPTIONS] BAM1_1[,BAM1_2,BAM1_3,...] BAM2_1[,BAM2_2,BAM2_3,...]
```

Get available options:

```
java -jar jacusa.jar call-2
```

Example gDNA vs. cDNA
---------------------

Download and extract sample data 

```
open https://cloud.dieterichlab.org/index.php/s/349PMjCdJl4wUwV
get hg19_chr1_gDNA_VS_cDNA.tar.gz
and unpack with
tar xzvpf hg19_chr1_gDNA_VS_cDNA.tar.gz
```

Call RNA-DNA differences (RDDs) by comparing gDNA and cDNA in sample data and save results in rdds.out.

```
$ java -jar call-2 -P U,S -a H,M,B,Y -f 1024 -T 2.3	-p 2 -r rdds.out gDNA.bam cDNA1.bam,cDNA2.bam
```

JacusaHelper
============

Read, Process, and write JACUSA output files 

Installation
------------

Download JacusaHelper: 

```
$ wget [https://github.com/dieterich-lab/JACUSA/tree/master/JacusaHelper/build/JacusaHelper_0.42.tar.gz](https://github.com/dieterich-lab/JACUSA/tree/master/JacusaHelper/build/JacusaHelper_0.42.tar.gz)
```

Install JacusaHelper in R:

```
install.packages("JacusaHelper_0.42.tar.gz")
library("JacusaHelper")
```

Example
-------

Load JacusaHelper package in R:

```
library("JacusaHelper")
```

Read JACUSA output, filter sites where the variant base is NOT present in all replicates of at least one sample, and finally add editing frequency info:

```
# Read Jacusa output and filter by test-statistic >= 1.56 and 
# ensure that site have at least 10 reads in (cov1) sample 1 and at least 5 reads per replicate in (covs2) sample 2
data <- Read("Jacusa_RDD.out, stat = 1.56, fields = c("cov1", "covs2"), cov = c(10, 5))
# This ensures that the variant base is present in all replicates of at least one sample
data <- FilterResult(data)
# This is only applicable for RDD calls and it will calculate their editing frequency.
# It is expected that gDNA is stored as sample 1!
data <- AddEditingFreqInfo(data)
```

Plot base change conversion:

```
# Among other additional infos, AddEditingFreqInfo will populate baseChange field in data
tbl <- table(data$baseChange)
barplot(tbl)
```

Check documentation in R for more details
```
?JacusaHelper.
```

AddVariants
===========

Add variants to a BAM file

Download
--------

Get the current AddVariants JAR:

```
$ wget [https://github.com/dieterich-lab/JACUSA/blob/master/tools/AddVariants/build/AddVariants_v0.3.jar](https://github.com/dieterich-lab/JACUSA/blob/master/tools/AddVariants/build/AddVariants_v0.3.jar)
```

Usage
-----

Implant variants defined in `<input.bam>` into `<variants.bed>` and write results to `<output.sam>`:

```
java -jar AddVariants.jar <input.bam> <variants.bed> | samtools view -Sb - > <output.sam>
```

Format of variants.bed
----------------------
chr | start | end

License
-------

see LICENSE file
