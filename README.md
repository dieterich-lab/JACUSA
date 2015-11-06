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
$ wget http://www.age.mpg.com/software/jacusa/current/JACUSA.jar
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
$ wget http://www.age.mpg.com/software/jacusa/sample_data/hg19_chr1_gDNA_VS_cDNA.tar.gz
$ tar xzvpf hg19_chr1_gDNA_VS_cDNA.tar.gz
```

Call RNA-DNA differences (RRDs) by comparing gDNA and cDNA in sample data and save results in rdds.out.

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
$ wget https://github.com/dieterich-lab/JACUSA/tree/master/JacusaHelper/release/JacusaHelper_0.2.tar.gz
```

Install JacusaHelper in R:

```
install.packages("JacusaHelper_0.2.tar.gz")
library("JacusaHelper")
```

Example
-------

Load JacusaHelper package in R:

```
library("JacusaHelper")
```

Read JACUSA output and add editing frequency info:

```
data <- read.table("Jacusa_RDD.out")
data <- AddEditingFreqInfo(data)
```

Plot base change conversion:

```
tbl <- Table(data)
barplot(tbl)
```

AddVariants
===========

Add variants to a BAM file

Download
--------

Get the current AddVariants JAR:

```
$ wget http://www.age.mpg.com/software/jacusa/current/AddVariants.jar
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

TODO