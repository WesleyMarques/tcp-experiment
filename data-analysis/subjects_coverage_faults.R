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
projectOne <- "la4j"
covOne <- "statement"
for(projectOne in projects){
  for(covOne in coverage){
    subjectsData %>%
      # filter(coverage == covOne, project == projectOne) %>%
      # filter(project == c("la4j", "jopt-simple")) %>%
      mutate(metric = (coveredTax - faultsTax)) %>%
      # filter(metric <= 0 ) %>%
      ggplot(aes(y = metric, x = test)) +
      geom_line(group=1)+
      geom_line(aes(y=median(metric)), group=1, color="red") + 
      ylab("covered - faults")+
      xlab("test name")+
      facet_wrap(~project, scales = ("free_x"))
  }
}

tempData <- subjectsData %>%
  mutate(metric = (coveredTax - faultsTax)) %>%
  group_by(project) %>%
  summarise(Med = median(metric))
length(unique(tempData$test))
