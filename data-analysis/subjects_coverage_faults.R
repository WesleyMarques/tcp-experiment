require(dplyr)
require(ggplot2)
require(readr)

subjectsData <- read_csv("data/result.data");
projects =  unique(subjectsData$project)
coverage = unique(subjectsData$coverage)

subjectsMedian <- subjectsData %>%
  group_by(project, version, coverage) %>%
  summarise(faultsMedian = median(faults), coveredMedian = median(covered)) %>%
  View()

for(projectOne in projects){
  for(covOne in coverage){
    subjectsData %>%
      filter(coverage == covOne, project == projectOne) %>%
      mutate(metric = (covered - faults)) %>%
      # filter(metric <= 0 ) %>%
      ggplot(aes(y = metric, x = testName)) +
      geom_line(group=1)+
      ylab("covered - faults")+
      xlab("test name")+
      facet_wrap(~version, scales = "free")
  }
}