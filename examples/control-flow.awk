BEGIN {
  total = 0
  i = 1

  while (i <= 5) {
    total = total + i
    i = i + 1
  }

  print "while-total", total

  for (j = 1; j <= 4; j = j + 1) {
    if (j == 3) {
      continue
    }
    print "loop", j
  }

  k = 0
  while (1) {
    k = k + 1
    if (k == 3) {
      break
    }
  }

  print "break-at", k
}
