d <- read.csv("meta/data.csv")
png("mygraph.png")
par(mar = c(15, 4, .5, .5))
barplot(setNames(dat$number, dat$name), las = 2)
dev.off()


d <- read.csv("meta/\"data\".csv")
png("mygraph\'s")
par(mar = c(15, 4, .5, .5))
barplot(setNames(dat$number, dat$name), las = 2)
dev.off()
