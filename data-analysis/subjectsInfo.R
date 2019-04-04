require(jsonlite)
require(dplyr)
require(ggplot2)
require(readr)
require(coin)
library(resample)

require(rcompanion)
require(multcompView)
library(FSA)
require(DescTools)

require(tibble)
library(broom)
library(cluster)
library(ggdendro)
require(tidyverse, quietly = TRUE, warn.conflicts = FALSE)
require(PMCMR)

colorder <- c( "blue", "green", "orange", "red", "brown")
algorithm_list <- c("Search-Based-statement","Search-Based-branch","Search-Based-method", "Total-statement","Total-method","Total-branch","Additional-statement","Additional-method","Additional-branch","ART-statement","ART-method","ART-branch")

data_subjects <- read_csv("data/dataframe_subjects.data")
data_subjects$algorithm = factor(data_apfd$algorithm, levels=algorithm_list)

projects =  unique(data_mSpreading$project)
coverage = unique(data_mSpreading$covLevel)

for(projectName in projects){
  data_subjects_project <- data_subjects %>%
    filter(project == projectName)
  data_subjects %>%
    # filter(covLevel == "statement") %>%
    # mutate(project = paste(project,"(stmt)", sep="")) %>%
    ggplot(aes(x = version)) +
    aes(color=factor(coverage),group=coverage)+
    geom_boxplot(aes(y = covered),outlier.colour="red")+
    geom_boxplot(aes(y = uncovered),outlier.colour="blue")+
    # geom_line(aes(y = uncovered))+
    ylab("APFD")+
    xlab("Versions")+
    # scale_fill_manual(values=colorder,name="Algorithms")+
    theme(axis.text.x = element_blank()) +
    facet_wrap(~project, scales = "free")
  graphName <- paste(c("graphics/apfd_mspreading_subjects/", project, ".pdf"), sep="", collapse = "")
  ggsave(graphName)
}


data_subjects %>%
  # filter(covLevel == "statement") %>%
  # mutate(project = paste(project,"(stmt)", sep="")) %>%
  ggplot(aes(x = version)) +
  aes(color=factor(coverage),group=coverage)+
  geom_boxplot(aes(y = covered),outlier.colour="red")+
  geom_boxplot(aes(y = uncovered),outlier.colour="blue")+
  # geom_line(aes(y = uncovered))+
  ylab("APFD")+
  xlab("Versions")+
  # scale_fill_manual(values=colorder,name="Algorithms")+
  theme(axis.text.x = element_blank()) +
  facet_wrap(~project, scales = "free")




