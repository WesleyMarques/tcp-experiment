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
# algorithm_list <- c("ART-statement","ART-branch","ART-method", "Total-statement","Total-method","Total-branch","Additional-statement","Additional-method","Additional-branch","Search-Based-statement","Search-Based-method","Search-Based-branch","AdditionalSimilarity-statement","AdditionalSimilarity-method","AdditionalSimilarity-branch")
algorithm_list <- c("AdditionalTotal-statement","AdditionalTotal-branch","AdditionalTotal-method", "Total-statement","Total-method","Total-branch","Additional-statement","Additional-method","Additional-branch","AdditionalSimilarity-statement","AdditionalSimilarity-method","AdditionalSimilarity-branch")
colorder <- c( "blue", "green", "orange", "red", "brown")

data_apfd <- read_csv("data/dataframe_apfd.data")
data_apfd$algorithm = factor(data_apfd$algorithm, levels=algorithm_list)

data_apfd %>%
  group_by(project, version) %>%
  summarise(count = n()/12) %>%
  summarise(max = max(count), min = min(count)) %>%
  View()

#Distribuição dos grupos de faltas para cada tipo de cobertura
data_apfd %>%
  filter(covLevel == "statement") %>%
  mutate(project = paste(project,"(stmt)", sep="")) %>%
  ggplot(aes(y=apfd, fill=algorithm)) +
  geom_boxplot(aes(x = version),outlier.colour="red")+
  ylab("APFD")+
  xlab("Versions")+
  scale_fill_manual(values=colorder,name="Algorithms")+
  facet_wrap(~project, scales = "free")

data_apfd %>%
  filter(covLevel == "method") %>%
  mutate(project = paste(project,"(method-cov)", sep="")) %>%
  ggplot(aes(y=apfd, fill=algorithm)) +
  geom_boxplot(aes(x = version),outlier.colour="red")+
  ylab("apfd")+
  xlab("Versions")+
  scale_fill_manual(values=colorder,name="Algorithms")+
  facet_wrap(~project, scales = "free")

data_apfd %>%
  filter(covLevel == "branch") %>%
  mutate(project = paste(project,"(branch-cov)", sep="")) %>%
  ggplot(aes(y=apfd, fill=algorithm)) +
  geom_boxplot(aes(x = version),outlier.colour="red")+
  ylab("apfd")+
  xlab("Versions")+
  scale_fill_manual(values=colorder,name="Algorithms")+
  facet_wrap(~project, scales = "free")

#Teste de normalidade
pearson.test(data_apfd$apfd, adjust = F)
qqnorm(data_apfd$apfd)

#Teste de similaridade de distribuição com Kruskal-Wallis para ca projeto
for(proj in unique(data_apfd$project)){
  dataTestApfd <- data_apfd %>%
    filter(project==proj) %>%
    mutate(algorithm = factor(algorithm, levels=unique(algorithm)))
  kruskal.test(apfd ~ algorithm, data=dataTestApfd)
  PT = dunnTest(apfd ~ algorithm, data=dataTestApfd, method = "bh", two.sided = F)
  PT = PT$res
  
  result <- cldList(comparison = PT$Comparison,
                    p.value = PT$P.adj,
                    threshold = 0.05)
  print(proj)
  print(result)
}

#Gráfico das diferenças de distribuição 
Sum = groupwiseMedian(apfd ~ algorithm,
                      data       = dataTestApfd,
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
result_apfd = data.frame(project=character(),
                         algorithm=character(),
                         X2.5.=double(),
                         x97.5.=double(),
                         stringsAsFactors = FALSE,
                         covType=character())

#Geração dos intervalos de confiaça para cada nível de cobertura
for(proj in unique(data_apfd$project)){
  for(alg in unique(data_apfd$algorithm)){
    result <- data_apfd %>%
      filter(project==proj, algorithm==alg) %>%
      resample::bootstrap(median(apfd), R = 10000) %>%
      CI.percentile(probs = c(.025, .975))
    result <- data.frame(result)
    result$algorithm = alg
    result$project = proj
    result$covType = gsub("\\S*-", "", alg)
    result_apfd <- rbind(result_apfd, result)
  }
}

colorder <- c( "blue","blue","blue", "green","green","green", "orange","orange","orange", "red","red","red", "brown","brown","brown")
result_apfd$algorithm = factor(result_apfd$algorithm, levels=algorithm_list)
result_apfd$covType = factor(result_apfd$covType, levels=c("statement","branch","method"))
result_apfd <- result_apfd %>%
  mutate(xMedian=(X2.5.+X97.5.)/2, algBase = gsub("-\\S*", "", algorithm))

result_apfd %>%
  group_by(algorithm) %>%
  summarise(value = max(xMedian)) %>%
  View()

result_apfd %>%
  ggplot(aes(x = algorithm, ymin = X2.5., ymax = X97.5., shape=factor(covType)))+
  geom_errorbar(aes(width = 1, color=algorithm), size=0.5, position=position_dodge(width = 0.8)) +
  geom_point(aes(y=xMedian, shape=factor(covType)), position = position_dodge(width = 0.8))+
  scale_color_manual(values=colorder,name="Algorithms")+
  theme(axis.title.x=element_blank(),
        axis.text.x=element_blank(),
        axis.ticks.x=element_blank())+
  ylab("APFD per Coverage Type")+
  xlab("Projects")+
  labs(shape="Coverage Type")+
  facet_wrap(~project, scales = "free")


temp <- result_apfd %>%
  filter(project == "la4j") %>% 
  arrange(desc(X97.5.))

rank <- function(data){
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
  last_x2 <- tempLine$X2.5.
  for(i in 2:nrow(data)){
    tempLine <- data[i,];
    if(tempLine$X97.5. < last_x2){
      cont <- cont + 1
      last_x2 <- tempLine$X2.5.
    }
    result_rank_apfd <- rbind(result_rank_apfd, data.frame(project=tempLine$project,
                                                           algorithm=tempLine$algorithm,
                                                           x1=tempLine$X2.5.,
                                                           x2=tempLine$X97.5.,
                                                           covType=tempLine$covType,
                                                           rank=cont))
  }
  return(result_rank_apfd)
}


for(proj in unique(data_apfd$project)){
  projectOrdered <- result_apfd %>%
    filter(project == proj) %>% 
    arrange(desc(X97.5.))
  print("==============================================")
  print(paste("===============", proj, "=============="))
  print("==============================================")
  temp_result <- rank(projectOrdered)
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





