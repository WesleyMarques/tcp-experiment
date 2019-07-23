require(dplyr)
require(ggplot2)
require(readr)

data_mSpreading <- read_csv("data/dataframe_mean-spreading.data")
data_apfd <- read_csv("data/dataframe_apfd.data")
projects =  unique(data_mSpreading$project)
coverage = unique(data_mSpreading$covLevel)

data_apfd <- data_apfd %>%
  mutate(metricName = "apfd")

data_mSpreading <- data_mSpreading %>%
  mutate(metricName = "meanSpreading") %>%
  mutate(metric = 1-metric)
for(projectOne in projects){
  data_apfd_project <- data_apfd %>%
    filter(project == projectOne)
  data_mSpreading_project <- data_mSpreading %>%
    filter(project == projectOne)
  for(covOne in coverage){
    data_apfd_project_cov <- data_apfd_project %>%
      filter(covLevel == covOne)
    data_mSpreading_project_cov <- data_mSpreading_project %>%
      filter(covLevel == covOne)
    data_full = data.frame(rbind(as.matrix(data_apfd_project_cov), as.matrix(data_mSpreading_project_cov)))
    data_full %>%
      mutate(metric = as.numeric(as.character(metric))) %>%
      group_by(metricName, algorithm, limiar, maxTests) %>%
      summarise(Mean=median(metric), Max = max(metric), Min=min(metric), Median=median(metric)) %>%
      ggplot(aes(x = algorithm, y = Median)) +
      aes(colour = factor(metricName),group = metricName)+
      geom_line()+
      # stat_summary(fun.y = mean, geom="line") +
      theme(axis.text.x = element_blank()) +
      # scale_y_discrete(breaks=c(0,1), labels=c(0,1))+
      scale_x_discrete(breaks=c(0,1), labels=c(0,1))+
      facet_wrap(~limiar, scales = "free")
    graphName <- paste(c("graphics/apfd_mspreading_line2/", projectOne, "_", covOne, ".pdf"), sep="", collapse = "")
    ggsave(graphName)
  }
}

data_apfd %>%
  mutate(algorithm = str_replace_all(algorithm, "GreedyAdditionalSimilarity", "GAS")) %>%
  # mutate(algorithm = str_replace_all(algorithm, "statement", "stmt")) %>%
  ggplot(aes(y=metric)) +
  aes(colour = factor(metricName),group = metricName)+
  geom_boxplot(aes(x = algorithm),outlier.colour="red")+
  ylab("APFD")+
  xlab("Versions")+
  theme(axis.text.x = element_blank())+
  facet_wrap(~limiar, scales = "free")


data_apfd %>%
  mutate(algorithm = str_replace_all(algorithm, "GreedyAdditionalSimilarity", "GAS")) %>%
  # mutate(algorithm = str_replace_all(algorithm, "statement", "stmt")) %>%
  ggplot(aes(y=metric)) +
  aes(colour = factor(metricName),group = metricName)+
  geom_boxplot(aes(x = algorithm),outlier.colour="red")+
  ylab("APFD")+
  xlab("Versions")+
  theme(axis.text.x = element_blank())+
  facet_wrap(~limiar, scales = "free")