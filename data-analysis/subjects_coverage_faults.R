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
      ggplot(aes(y = faults, x = testName)) +
      geom_line(group=1)+
      ylab("covered")+
      xlab("test name")+
      facet_wrap(~version, scales = "free")
    graphName <- paste(c("graphics/apfd_mspreading_covered_vs_faults/", projectOne, "_", covOne, "_faults.pdf"), sep="", collapse = "")
    ggsave(graphName)
  }
}
