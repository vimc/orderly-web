extract$number <- extract$number * 1.2
extract$number <- extract$number * 1.2
extract$number <- extract$number + rnorm(1)

write.csv(extract, "summary.csv", row.names = TRUE)

png("graph.png")
par(mar = c(15, 4, .5, .5))
do_plot(extract)
dev.off()
