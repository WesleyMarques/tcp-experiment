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
library(lattice)

require(tibble)
library(broom)
library(cluster)
library(ggdendro)
require(tidyverse, quietly = TRUE, warn.conflicts = FALSE)
require(PMCMR)

colorder <- c( "blue", "green", "orange", "red")

data_apfd_Spreading <- read_csv("data/dataframe_apfd_mSprading.data")
data_apfd_Spreading$algorithm = factor(data_apfd_Spreading$algorithm, levels=c("GreedyTotal","GreedyAdditional","ARTMaxMin","Genetic"))

data_mSpreading_filtered <- data_apfd_Spreading %>%
  filter(project == "assertj-core", covLevel=="statement", meanSpreading > 0)
histogram(~ meanSpreading | algorithm,
          data=data_mSpreading_filtered,
          layout=c(1,4))

Summarize(meanSpreading ~ algorithm,
          data=data_mSpreading_filtered, 
          digits=7)

data_apfd_Spreading %>%
  filter(covLevel == "statement") %>%
  mutate(project = paste(project,"(stmt-cov)", sep="")) %>%
  ggplot(aes(y=apfd, fill=algorithm)) +
  geom_boxplot(aes(x = version),outlier.colour="red")+
  ylab("Group-Spreading")+
  xlab("Versions")+
  scale_fill_manual(values=colorder,name="Algorithms")+
  facet_wrap(~project, scales = "free")

data_apfd_Spreading %>%
  ggplot(aes(x=apfd, color=algorithm)) +
  geom_density() +
  facet_wrap(~project)

for(proj in unique(data_apfd_Spreading$project)){
  dataTest <- data_apfd_Spreading %>%
    filter(covLevel == "statement", project==proj) %>%
    mutate(algorithm = factor(algorithm, levels=unique(algorithm)))
  kruskal.test(apfd ~ algorithm, data=dataTest)
  PT = dunnTest(apfd ~ algorithm, data=dataTest, method = "bh")
  
  newPT = PT$res
  
  result <- cldList(comparison = newPT$Comparison,
                    p.value = newPT$P.adj,
                    threshold = 0.05)
  print(proj)
  print(result)
}

result_mSpreading = data.frame(project=character(),
                               algorithm=character(),
                               X2.5.=double(),
                               x97.5.=double(),
                               stringsAsFactors = FALSE)

for(proj in unique(data_apfd_Spreading$project)){
  for(alg in unique(data_apfd_Spreading$algorithm)){
    result <- data_apfd_Spreading %>%
      filter(covLevel == "statement", project==proj, algorithm==alg) %>%
      resample::bootstrap(mean(apfd), R = 5000) %>%
      CI.percentile(probs = c(.025, .975))
    result <- data.frame(result)
    result$algorithm = alg
    result$project = proj
    result_mSpreading <- rbind(result_mSpreading, result)
  }
}

result_mSpreading$algorithm = factor(result_mSpreading$algorithm, levels=c("GreedyTotal","GreedyAdditional","ARTMaxMin","Genetic"))
result_mSpreading %>%
  ggplot(aes(x = algorithm, ymin = X2.5., ymax = X97.5., color=algorithm))+
  geom_errorbar(width = .4) +
  scale_color_manual(values=colorder,name="Algorithms")+
  theme(axis.title.x=element_blank(),
        axis.text.x=element_blank(),
        axis.ticks.x=element_blank())+
  facet_wrap(~project, scales = "free")

