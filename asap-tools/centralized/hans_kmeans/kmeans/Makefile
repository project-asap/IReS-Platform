benchmark=kmeans_arff

CXX=icc
CXXFLAGS+=-O3 -I..

all: $(benchmark)

kmeans_arff: kmeans_arff.cpp ../stddefines.h
	$(CXX) $(CXXFLAGS) -std=c++11 $^ -o $@ $(LDFLAGS)

clean:
	rm -fr $(benchmark)
