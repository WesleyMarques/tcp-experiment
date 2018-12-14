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

colorder <- c( "blue", "green", "orange", "red", "brown")
# algorithm_list <- c("ART-statement","ART-branch","ART-method", "Total-statement","Total-method","Total-branch","Additional-statement","Additional-method","Additional-branch","Search-Based-statement","Search-Based-method","Search-Based-branch","AdditionalNew-statement","AdditionalNew-method","AdditionalNew-branch")
algorithm_list <- c("AdditionalTotal-statement","AdditionalTotal-branch","AdditionalTotal-method", "Total-statement","Total-method","Total-branch","Additional-statement","Additional-method","Additional-branch","AdditionalNew-statement","AdditionalNew-method","AdditionalNew-branch")

data_mSpreading <- read_csv("data/dataframe_mean_spreading.data")
data_mSpreading$algorithm = factor(data_mSpreading$algorithm, levels=algorithm_list)

data_mSpreading %>%
  group_by(project, version, algorithm, covLevel) %>%
  summarise(count = n()) %>%
  View()

data_mSpreading <- data_mSpreading %>%
  filter(meanSpreading > 0)


data_mSpreading %>% 
  ggplot(aes(y=meanSpreading, fill=algorithm)) +
  geom_smooth(aes(x=project), size = 2, method = "lm", se = FALSE)

data_mSpreading %>%
  filter(covLevel == "statement") %>%
  mutate(project = paste(project,"(stmt-cov)", sep="")) %>%
  ggplot(aes(y=meanSpreading, fill=algorithm)) +
  geom_boxplot(aes(x = version),outlier.colour="red")+
  ylab("Mean-Spreading")+
  xlab("Versions")+
  scale_fill_manual(values=colorder,name="Algorithms")+
  facet_wrap(~project, scales = "free")

data_mSpreading %>%
  filter(covLevel == "statement") %>%
  mutate(project = paste(project,"(stmt)", sep="")) %>%
  ggplot(aes(y=meanSpreading, fill=algorithm)) +
  geom_boxplot(aes(x = version),outlier.colour="red")+
  ylab("APFD")+
  xlab("Versions")+
  scale_fill_manual(values=colorder,name="Algorithms")+
  facet_wrap(~project, scales = "free")

data_mSpreading %>%
  filter(covLevel == "branch") %>%
  mutate(project = paste(project,"(branch-cov)", sep="")) %>%
  ggplot(aes(y=meanSpreading, fill=algorithm)) +
  geom_boxplot(aes(x = version),outlier.colour="red")+
  ylab("Mean-Spreading")+
  xlab("Versions")+
  scale_fill_manual(values=colorder,name="Algorithms")+
  facet_wrap(~project, scales = "free")

data_mSpreading %>%
  filter(covLevel == "method") %>%
  mutate(project = paste(project,"method-cov)", sep="")) %>%
  ggplot(aes(y=meanSpreading, fill=algorithm)) +
  geom_boxplot(aes(x = version),outlier.colour="red")+
  ylab("Mean-Spreading")+
  xlab("Versions")+
  scale_fill_manual(values=colorder,name="Algorithms")+
  facet_wrap(~project, scales = "free")

#Teste de normalidade
pearson.test(data_mSpreading$meanSpreading, adjust = F)
qqnorm(data_mSpreading$meanSpreading)

#Teste de similaridade de distribuição com Kruskal-Wallis para ca projeto
for(proj in unique(data_mSpreading$project)){
  dataTestmSpreading <- data_mSpreading %>%
    filter(project==proj) %>%
    mutate(algorithm = factor(algorithm, levels=unique(algorithm)))
  kruskal.test(meanSpreading ~ algorithm, data=dataTestmSpreading)
  PT = dunnTest(meanSpreading ~ algorithm, data=dataTestmSpreading, method = "bh")
  
  newPT = PT$res
  
  result <- cldList(comparison = newPT$Comparison,
                    p.value = newPT$P.adj,
                    threshold = 0.05)
  print(proj)
  print(result)
}

#Gráfico das diferenças de distribuição 
Sum = groupwiseMedian(mSpreading ~ algorithm,
                      data       = dataTestmSpreading,
                      R          = 5000,
                      percentile = TRUE,
                      bca        = TRUE,
                      digits     = 3)
Sum$algorithm = factor(Sum$algorithm, levels=c("ARTMaxMin","Genetic","GreedyAdditional","GreedyTotal"))
X = 1:4
Y = Sum$Percentile.upper + 0.01
temp = Y[4]
Y[4] = Y[3]
Y[3] = temp
Label = result$Letter
ggplot(Sum,
       aes(x = algorithm,
           y = Median)) +
  geom_errorbar(aes(ymin = Percentile.lower,
                    ymax = Percentile.upper),
                width = 0.05,
                size  = 0.5) +
  geom_point(shape = 15,
             size  = 4) +
  theme_bw() +
  theme(axis.title = element_text(face  = "bold")) +
  ylab(proj) +
  annotate("text",
           x = X,
           y = Y,
           label = result$Letter)

#Dataframes para os gráficos dos Intervalos de confiança
result_mSpreading = data.frame(project=character(),
                         algorithm=character(),
                         X2.5.=double(),
                         x97.5.=double(),
                         stringsAsFactors = FALSE,
                         covType=character())

#Geração dos intervalos de confiaça para cada nível de cobertura
  for(proj in unique(data_mSpreading$project)){
    for(alg in unique(data_mSpreading$algorithm)){
      result <- data_mSpreading %>%
        filter(project==proj, algorithm==alg) %>%
        resample::bootstrap(mean(meanSpreading), R = 5000) %>%
        CI.percentile(probs = c(.025, .975))
      result <- data.frame(result)
      result$algorithm = alg
      result$project = proj
      result$covType = gsub("\\S*-", "", alg)
      result_mSpreading <- rbind(result_mSpreading, result)
    }
  }

#Data frame dos intervalos de confiança com todos os critérios de cobertura
colorder <- c( "blue","blue","blue", "green","green","green", "orange","orange","orange", "red","red","red", "brown", "brown", "brown")
result_mSpreading$algorithm = factor(result_mSpreading$algorithm, levels=algorithm_list)
result_mSpreading %>%
  ggplot(aes(x = algorithm, ymin = X2.5., ymax = X97.5., shape=factor(covType)))+
  geom_errorbar(aes(width = 1, color=algorithm), size=0.5, position=position_dodge(width = 0.8)) +
  geom_point(aes(y=(X2.5.+X97.5.)/2, shape=factor(covType)), position = position_dodge(width = 0.8))+
  scale_color_manual(values=colorder,name="Algorithms")+
  theme(axis.title.x=element_blank(),
        axis.text.x=element_blank(),
        axis.ticks.x=element_blank())+
  ylab("Mean-Spreading per Coverage Type")+
  xlab("Projects")+
  labs(shape="Coverage Type")+
  facet_wrap(~project)


rank_mSpreadning <- function(data){
  result_rank_apfd = data.frame(project=character(),
                                algorithm=character(),
                                X2.5.=double(),
                                x97.5.=double(),
                                stringsAsFactors = FALSE,
                                covType=character(),
                                rank=numeric())
  cont <- 1
  tempLine <- data[1,];
  result_rank_apfd <- rbind(result_rank_apfd, data.frame(project=tempLine$project,
                                                         algorithm=tempLine$algorithm,
                                                         x1=tempLine$X2.5.,
                                                         x2=tempLine$X97.5.,
                                                         covType=tempLine$covType,
                                                         rank=cont))
  last_x2 <- tempLine$X97.5.
  for(i in 2:nrow(data)){
    tempLine <- data[i,];
    if(tempLine$X2.5. > last_x2){
      cont <- cont + 1
      last_x2 <- tempLine$X97.5.
    }w
    result_rank_apfd <- rbind(result_rank_apfd, data.frame(project=tempLine$project,
                                                           algorithm=tempLine$algorithm,
                                                           x1=tempLine$X2.5.,
                                                           x2=tempLine$X97.5.,
                                                           covType=tempLine$covType,
                                                           rank=cont))
  }
  return(result_rank_apfd)
}


for(proj in unique(result_mSpreading$project)){
  projectOrdered <- result_mSpreading %>%
    mutate(mspre)
    filter(project == proj) %>% 
    arrange(X2.5.)
  print("==============================================")
  print(paste("===============", proj, "=============="))
  print("==============================================")
  temp_result <- rank_mSpreadning(projectOrdered)
  result_str <- temp_result[1,]$algorithm
  last_rank <- temp_result[1,]$rank
  for(i in 2:nrow(temp_result)){
    if(last_rank != temp_result[i,]$rank){
      last_rank = temp_result[i,]$rank
      result_str <- paste(result_str, " $>$ ", temp_result[i,]$algorithm)
    }else{
      result_str <- paste(result_str, " = ", temp_result[i,]$algorithm)
    }
  }
  print(result_str)
}