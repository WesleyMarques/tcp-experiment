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

colorder <- c( "blue", "green", "orange", "red")

data_spreadind <- read_csv("data/dataframe_spreading.data")
data_spreadind$algorithm = factor(data_spreadind$algorithm, levels=c("GreedyTotal","GreedyAdditional","ARTMaxMin","Genetic"))
data_group_spreadind <- read_csv("data/dataframe_group_spreading.data")
data_group_spreadind$algorithm = factor(data_group_spreadind$algorithm, levels=c("GreedyTotal","GreedyAdditional","ARTMaxMin","Genetic"))

summary(data_apfd)
summary(data_spreadind)
summary(data_group_spreadind)





data_spreadind %>%
    filter(spreading > 0, covLevel == "statement") %>%
    mutate(project = paste(project,"(stmt-cov)", sep="")) %>%
    ggplot(aes(y=spreading, fill=algorithm)) +
    geom_boxplot(aes(x = version),outlier.colour="red")+
    ylab("Spreading")+
    xlab("Versions")+
    scale_fill_manual(values=colorder,name="Algorithms")+
    facet_wrap(~project, scales = "free")

data_spreadind %>%
    filter(spreading > 0) %>%
    ggplot(aes(x=spreading, color=algorithm)) +
    geom_density() +
    facet_wrap(~project)

data_group_spreadind %>%
  filter(gspreading > 0, covLevel == "statement") %>%
  mutate(version = substr(version,0, 6)) %>%
  mutate(project = paste(project,"(stmt-cov)", sep="")) %>%
  ggplot(aes(y=gspreading, fill=algorithm)) +
  geom_boxplot(aes(x = version),outlier.colour="red")+
  ylab("Group-Spreading")+
  xlab("Versions")+
  scale_fill_manual(values=colorder,name="Algorithms")+
  facet_wrap(~project, scales = "free")

data_group_spreadind %>%
  filter(gspreading > 0) %>%
  ggplot(aes(x=gspreading, color=algorithm)) +
  geom_density() +
  facet_wrap(~project)

for(proj in unique(data_group_spreadind$project)){
  dataTest <- data_group_spreadind %>%
    filter(covLevel == "statement", project==proj) %>%
    mutate(algorithm = factor(algorithm, levels=unique(algorithm)))
  kruskal.test(gspreading ~ algorithm, data=dataTest)
  PT = dunnTest(gspreading ~ algorithm, data=dataTest, method = "bh")
  PT = PT$res
  result <- cldList(comparison = PT$Comparison,
                    p.value = PT$P.adj,
                    threshold = 0.05)
  print(proj)
  print(result)
}
dataTest <- data_group_spreadind %>%
  filter(covLevel == "statement")
beanplot(gspreading ~ algorithm, data=dataTest, log="",col="white",method="jitter")

data_spreadind %>%
    filter(spreading > 0, covLevel == "method") %>%
    mutate(version = substr(version,0, 6)) %>%
    mutate(project = paste(project,"(meth-cov)", sep="")) %>%
    ggplot(aes(y=spreading, fill=algorithm)) +
    geom_boxplot(aes(x = version),outlier.colour="red")+
    ylab("Spreading")+
    xlab("Versions")+
    scale_fill_manual(values=colorder,name="Algorithms")+
    theme(axis.text.x = element_text(angle=45, hjust = 0.5, vjust = 0.5))+
    facet_wrap(~project, scales = "free")

for(proj in unique(data_spreadind$project)){
  dataTest <- data_spreadind %>%
    filter(covLevel == "statement", project==proj) %>%
    mutate(algorithm = factor(algorithm, levels=unique(algorithm)))
  kruskal.test(spreading ~ algorithm, data=dataTest)
  PT = dunnTest(spreading ~ algorithm, data=dataTest, method = "bh")
  PT = PT$res
  result <- cldList(comparison = PT$Comparison,
          p.value = PT$P.adj,
          threshold = 0.05)
  print(proj)
  print(result)
}
dataTest <- data_spreadind %>%
  filter(covLevel == "statement")
beanplot(spreading ~ algorithm, data=dataTest, log="",col="white",method="jitter")


# Calcular IC


result_spreading = data.frame(project=character(),
                              algorithm=character(),
                              X2.5.=double(),
                              x97.5.=double(),
                              stringsAsFactors = FALSE)

for(proj in unique(data_spreadind$project)){
  for(alg in unique(data_spreadind$algorithm)){
    result <- data_spreadind %>%
      filter(covLevel == "statement", project==proj, algorithm==alg) %>%
      select(spreading) %>%
      resample::bootstrap(mean(spreading), R = 5000) %>%
      CI.percentile(probs = c(.025, .975))
    result <- data.frame(result)
    result$algorithm = alg
    result$project = proj
    result_spreading <- rbind(result_spreading, result)
  }
}

result_spreading$algorithm = factor(result_spreading$algorithm, levels=c("GreedyTotal","GreedyAdditional","ARTMaxMin","Genetic"))
result_spreading %>%
  ggplot(aes(x = algorithm, ymin = X2.5., ymax = X97.5., color=algorithm))+
  geom_errorbar(width = .4) +
  scale_color_manual(values=colorder,name="Algorithms")+
  theme(axis.title.x=element_blank(),
        axis.text.x=element_blank(),
        axis.ticks.x=element_blank())+
  facet_wrap(~project, scales = "free")

result_group_spreading = data.frame(project=character(),
                              algorithm=character(),
                              X2.5.=double(),
                              x97.5.=double(),
                              stringsAsFactors = FALSE)

for(proj in unique(data_group_spreadind$project)){
  for(alg in unique(data_group_spreadind$algorithm)){
    result <- data_group_spreadind %>%
      filter(covLevel == "statement", project==proj, algorithm==alg, gspreading > 0) %>%
      select(gspreading) %>%
      resample::bootstrap(mean(gspreading), R = 5000) %>%
      CI.percentile(probs = c(.025, .975))
    result <- data.frame(result)
    result$algorithm = alg
    result$project = proj
    result_group_spreading <- rbind(result_group_spreading, result)
  }
}

result_group_spreading$algorithm = factor(result_group_spreading$algorithm, levels=c("GreedyTotal","GreedyAdditional","ARTMaxMin","Genetic"))
result_group_spreading %>%
  ggplot(aes(x = algorithm, ymin = X2.5., ymax = X97.5., color=algorithm))+
  geom_errorbar(width = .4) +
  scale_color_manual(values=colorder,name="Algorithms")+
  theme(axis.title.x=element_blank(),
        axis.text.x=element_blank(),
        axis.ticks.x=element_blank())+
  facet_wrap(~project, scales = "free")


#Análise descritiva

data_spreadind %>%
  filter(spreading > 0, covLevel=="statement") %>%
  ggplot(aes(x=project, fill=project))+
  theme(axis.text.x=element_text(angle = 45, hjust = 1),
        axis.ticks.x=element_blank())+
  labs(x="Projects", y="Number of Faults")+
  scale_fill_grey()+
  geom_bar()