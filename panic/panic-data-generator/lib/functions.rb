
# AbstractFunction is the superclass of any other function class
class AbstractFunction
  attr_accessor :range
  def initialize()
  end
  
end


class Linear < AbstractFunction
  
  def initialize()
    super()
  end
end 