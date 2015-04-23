#!/usr/bin/ruby

  
require_relative 'functions'


[1, 2, 4, 8].each { |x1|
  (1..20).each { |x2|
    puts "#{x1}\t#{x2}\t#{((x1-1)/(8.0-1)+((x2-1)/(20.0-1.0)))*9000 + 1000}"
  }
}