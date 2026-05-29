BEGIN {
  print "name", "score"
}

$2 >= 80 {
  print $1, $2
}

END {
  print "done"
}
