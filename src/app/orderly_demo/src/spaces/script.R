d <- read.csv("a resource with spaces.csv")
png("a graph with spaces.png")
par(mar = c(15, 4, .5, .5))
barplot(setNames(dat$number, dat$name), las = 2)
dev.off()


d <- read.csv("some\"data\".csv")
png("mygraph\'s.png")
par(mar = c(15, 4, .5, .5))
barplot(setNames(dat$number, dat$name), las = 2)
dev.off()
