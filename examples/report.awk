BEGIN {
  print "student", "grade", "status"
}

$2 >= 90 {
  print toupper($1), $2, "honors"
}

$2 >= 80 && $2 < 90 {
  print $1, $2, "pass"
}

END {
  print "report complete"
}
