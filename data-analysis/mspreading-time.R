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

data_mSpreading <- read_csv("data/dataframe_time.data")
data_mSpreading$algorithm = factor(data_mSpreading$algorithm, levels=c("GreedyTotal","GreedyAdditional","ARTMaxMin","Genetic"))

data_mSpreading_filtered <- data_mSpreading %>%
  filter(project == "assertj-core", covLevel=="statement", meanSpreadingTime > 0)
histogram(~ meanSpreadingTime | algorithm,
          data=data_mSpreading_filtered,
          layout=c(1,4))

Summarize(meanSpreadingTime ~ algorithm,
          data=data_mSpreading_filtered, 
          digits=7)

data_mSpreading %>%
  filter(covLevel == "statement") %>%
  mutate(project = paste(project,"(stmt-cov)", sep="")) %>%
  ggplot(aes(y=time, fill=algorithm)) +
  geom_boxplot(aes(x = version),outlier.colour="red")+
  ylab("Group-Spreading")+
  xlab("Versions")+
  scale_fill_manual(values=colorder,name="Algorithms")+
  facet_wrap(~project, scales = "free")

data_mSpreading %>%
  ggplot(aes(x=meanSpreadingTime, color=algorithm)) +
  geom_density() +
  facet_wrap(~project)

for(proj in unique(data_mSpreading$project)){
  dataTest <- data_mSpreading %>%
    filter(covLevel == "statement", project==proj) %>%
    mutate(algorithm = factor(algorithm, levels=unique(algorithm)))
  kruskal.test(meanSpreading ~ algorithm, data=dataTest)
  PT = dunnTest(meanSpreading ~ algorithm, data=dataTest, method = "bh")
  
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

for(proj in unique(data_mSpreading$project)){
  for(alg in unique(data_mSpreading$algorithm)){
    result <- data_mSpreading %>%
      filter(covLevel == "statement", algorithm==alg) %>%
      resample::bootstrap(median(time), R = 5000) %>%
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
        axis.ticks.x=element_blank())
+facet_wrap(~project, scales = "free")

