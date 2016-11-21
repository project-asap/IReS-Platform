#include <sys/mman.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <iostream>
#include <stdio.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdlib.h>
#include <stdint.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <cilk/cilk_api.h>
#include <cerrno>
#include <cmath>
#include <limits>
#include <cassert>
#include <vector>

#if SEQUENTIAL && PMC
#include <likwid.h>
#endif

#if !SEQUENTIAL
#include <cilk/cilk.h>
#include <cilk/reducer.h>
#else
#define cilk_sync
#define cilk_spawn
#define cilk_for for
#endif

#if TRACING
#include "tracing/events.h"
#include "tracing/events.cc"
#endif

#include "stddefines.h"

#define DEF_NUM_POINTS 100000
#define DEF_NUM_MEANS 100
#define DEF_DIM 3
#define DEF_GRID_SIZE 1000
#define DEF_NUM_THREADS 8

#define REAL_IS_INT 0
typedef float real;

int num_points; // number of vectors
int num_dimensions;         // Dimension of each vector
int num_clusters; // number of clusters
real * min_val; // min value of each dimension of vector space
real * max_val; // max value of each dimension of vector space
const char * infile = NULL; // input file

#define CROAK(x)   croak(x,__FILE__,__LINE__)

inline void __attribute__((noreturn))
croak( const char * msg, const char * srcfile, unsigned lineno ) {
    const char * es = strerror( errno );
    std::cerr << srcfile << ':' << lineno << ": " << msg
	      << ": " << es << std::endl;
    exit( 1 );
}

struct point
{
    real * d;
    int cluster;

    point() { d = NULL; cluster = -1; }
    point(real* d, int cluster) { this->d = d; this->cluster = cluster; }
    
    bool normalize() {
	if( cluster == 0 ) {
	    std::cerr << "empty cluster...\n";
	    return true;
	} else {
#if VECTORIZED
	    d[0:num_dimensions] /= (real)cluster;
#else
	    for(int i = 0; i < num_dimensions; ++i)
		d[i] /= (real)cluster;
#endif
	    return false;
	}
    }

    void clear() {
#if VECTORIZED
	d[0:num_dimensions] = (real)0;
#else
        for(int i = 0; i < num_dimensions; ++i)
	    d[i] = (real)0;
#endif
    }
    
#if VECTORIZED
    static unsigned real esqd( real a, real b ) {
	real diff = a - b;
need to adjust ...
	return diff * diff;
    }
    real sq_dist(point const& p) const {
	return __sec_reduce_add( esqd( d[0:num_dimensions],
				       p.d[0:num_dimensions] ) );
    }
#else
    real sq_dist(point const& p) const {
        real sum = 0;
        for (int i = 0; i < num_dimensions; i++) {
            real diff = d[i] - p.d[i];
	    // if( diff < (real)0 )
		// diff = -diff;
	    // diff = (diff - min_val[i]) / (max_val[i] - min_val[i] + 1);
            sum += diff * diff;
        }
        return sum;
    }
#endif
    
    void dump() const {
        for(int j = 0; j < num_dimensions; j++) {
#if REAL_IS_INT
	    printf("%5ld ", (long)d[j]);
#else
	    printf("%6.4f ", (double)d[j]);
#endif
	}
        printf("\n");
    }
    
    void generate() {
        for(int j = 0; j < num_dimensions; j++)
            // d[j] = (real)(min_val[j] + rand() % (unsigned long)(max_val[j]-min_val[j]+1));
            d[j] = (real)(rand() % (unsigned long)1000)/(real)1000;
    }

    // For reduction of centre computations
    const point & operator += ( const point & pt ) {
#if VECTORIZED
	d[0:num_dimensions] += pt.d[0:num_dimensions];
#else
        for(int j = 0; j < num_dimensions; j++)
	    d[j] += pt.d[j];
#endif
	cluster += pt.cluster;
	return *this;
    }
};
typedef struct point Point;

class Centres {
    Point * centres;
    real * data;

public:
    Centres() {
	// Allocate backing store and initalize to zero
	data = new real[num_clusters * num_dimensions]();
	centres = new Point[num_clusters];
	for( int c=0; c < num_clusters; ++c ) {
	    centres[c] = Point( &data[c*num_dimensions], 0 );
	    centres[c].cluster = 0;
	}
    }
    ~Centres() {
	delete[] centres;
	delete[] data;
    }

    void clear() {
	for( int c=0; c < num_clusters; ++c ) {
	    centres[c].clear();
	    centres[c].cluster = 0;
	}
    }
    void add_point( Point * pt ) {
	int c = pt->cluster;
	for( int i=0; i < num_dimensions; ++i )
	    centres[c].d[i] += pt->d[i];
	centres[c].cluster++;
    }

    void normalize( int c ) {
    	centres[c].normalize();
    }
    bool normalize() {
	bool modified = false;
	cilk_for( int c=0; c < num_clusters; ++c )
	    modified |= centres[c].normalize();
	return modified;
    }

    void generate() {
	for( int c=0; c < num_clusters; ++c )
	    centres[c].generate();
    }

    void select( const point * pts ) {
	for( int c=0; c < num_clusters; ) {
	    int pi = rand() % num_points;

	    // Check if we already have this point (may have duplicates)
	    bool incl = false;
	    for( int k=0; k < c; ++k ) {
		if( memcmp( centres[k].d, pts[pi].d,
			    sizeof(real) * num_dimensions ) ) {
		    incl = true;
		    break;
		}
	    }
	    if( !incl ) {
		for( int i=0; i < num_dimensions; ++i )
		    centres[c].d[i] = pts[pi].d[i];
		++c;
	    }
	}
    }


    const Point & operator[] ( int c ) const {
	return centres[c];
    }

    void reduce( Centres * cc ) {
	for( int c=0; c < num_clusters; ++c )
	    centres[c] += cc->centres[c];
    }

    void swap( Centres & c ) {
    	std::swap( data, c.data );
    	std::swap( centres, c.centres );
    }
};

#if !SEQUENTIAL
class centres_reducer {
    struct Monoid : cilk::monoid_base<Centres> {
	static void reduce( Centres * left, Centres * right ) {
#if TRACING
	    event_tracer::get().record( event_tracer::e_sreduce, 0, 0 );
#endif
	    left->reduce( right );
#if TRACING
	    event_tracer::get().record( event_tracer::e_ereduce, 0, 0 );
#endif
	}
    };

private:
    cilk::reducer<Monoid> imp_;

public:
    centres_reducer() : imp_() { }

    const Point & operator[] ( int c ) const {
	return imp_.view()[c];
    }

    void swap( Centres & c ) {
	imp_.view().swap( c );
    }

    void add_point( Point * pt ) {
	imp_.view().add_point( pt );
    }
};

#else
typedef Centres centres_reducer;
#endif

int kmeans_cluster(Centres & centres, Point * points) {
    int modified = 0;

    centres_reducer new_centres;

#if GRANULARITY
    int nmap = std::min(num_points, 16) * 16;
    int g = std::max(1, (int)((double)(num_points+nmap-1) / nmap));
#pragma cilk grainsize = g
    cilk_for(int i = 0; i < num_points; i++) {
#else
    cilk_for(int i = 0; i < num_points; i++) {
#endif
#if TRACING
	event_tracer::get().record( event_tracer::e_smap, 0, 0 );
#endif
        //assign points to cluster
        real smallest_distance = std::numeric_limits<real>::max();
        int new_cluster_id = -1;
        for(int j = 0; j < num_clusters; j++) {
            //assign point to cluster with smallest total squared difference (for all d dimensions)
            real total_distance = points[i].sq_dist(centres[j]);
            if(total_distance < smallest_distance) {
                smallest_distance = total_distance;
                new_cluster_id = j;
            }
        }

        //if new cluster then update modified flag
        if(new_cluster_id != points[i].cluster)
        {
	    // benign race; works well. Alternative: reduction(|: modified)
            modified = 1;
            points[i].cluster = new_cluster_id;
        }

	new_centres.add_point( &points[i] );
#if TRACING
	event_tracer::get().record( event_tracer::e_emap, 0, 0 );
#endif
    }

#if TRACING
    event_tracer::get().record( event_tracer::e_synced, 0, 0 );
#endif

/*
    cilk_for(int i = 0; i < num_clusters; i++) {
	if( new_centres[i].cluster == 0 ) {
	    cilk_for(int j = 0; j < num_dimensions; j++) {
		new_centres[i].d[j] = centres[i].d[j];
	    }
	}
    }
*/

    // for(int i = 0; i < num_clusters; i++) {
	// std::cout << "in cluster " << i << " " << new_centres[i].cluster << " points\n";
    // }

    new_centres.swap( centres );
    if( centres.normalize() )
	modified = true;
    return modified;
}

void parse_args(int argc, char **argv)
{
    int c;
    extern char *optarg;
    
    // num_points = DEF_NUM_POINTS;
    num_clusters = DEF_NUM_MEANS;
    // num_dimensions = DEF_DIM;
    // grid_size = DEF_GRID_SIZE;
    
    while ((c = getopt(argc, argv, "c:i:")) != EOF) 
    {
        switch (c) {
            // case 'd':
                // num_dimensions = atoi(optarg);
                // break;
            case 'c':
                num_clusters = atoi(optarg);
                break;
            case 'i':
                infile = optarg;
                break;
            // case 'p':
                // num_points = atoi(optarg);
                // break;
	    // case 's':
		// grid_size = atoi(optarg);
		// break;
            case '?':
                printf("Usage: %s -d <vector dimension> -c <num clusters> -p <num points> -s <max value> -t <number of threads>\n", argv[0]);
                exit(1);
        }
    }
    
    if( num_clusters <= 0 )
	CROAK( "Number of clusters must be larger than 0." );
    if( !infile )
	CROAK( "Input file must be supplied." );
    
    std::cerr << "Number of clusters = " << num_clusters << '\n';
    std::cerr << "Input file = " << infile << '\n';
}

struct arff_file {
    std::vector<const char *> idx;
    std::vector<Point> points;
    char * fdata;
    char * relation;
    real * minval, * maxval;

public:
    arff_file() { }

    void read_sparse_file( const char * fname ) {
	struct stat finfo;
	int fd;

	if( (fd = open( fname, O_RDONLY )) < 0 )
	    CROAK( fname );
	if( fstat( fd, &finfo ) < 0 )
	    CROAK( "fstat" );

	uint64_t r = 0;
	fdata = new char[finfo.st_size+1];
	while( r < (uint64_t)finfo.st_size )
	    r += pread( fd, fdata + r, finfo.st_size, r );
	fdata[finfo.st_size] = '\0';

	close( fd );

	// Now parse the data
	char * p = fdata, * q;
#define ADVANCE(pp) do { if( *(pp) == '\0' ) goto END_OF_FILE; ++pp; } while( 0 )
	do {
	    while( *p != '@' )
		ADVANCE( p );
	    ADVANCE( p );
	    if( !strncasecmp( p, "relation ", 9 ) ) {
		p += 9;
		while( *p == ' ' )
		    ADVANCE( p );
		relation = p;
		if( *p == '\'' ) { // scan until closing quote
		    ADVANCE( p );
		    while( *p != '\'' )
			ADVANCE( p );
		    ADVANCE( p );
		    ADVANCE( p );
		    *(p-1) = '\0';
		} else { // scan until space
		    while( !isspace( *p ) )
			ADVANCE( p );
		    ADVANCE( p );
		    *(p-1) = '\0';
		}
	    } else if( !strncasecmp( p, "attribute ", 10 ) ) {
		p += 10;
		// Isolate token
		while( isspace( *p ) )
		    ADVANCE( p );
		q = p;
		while( !isspace( *p ) )
		    ADVANCE( p );
		ADVANCE( p );
		*(p-1) = '\0';
		// Isolate type
		while( isspace( *p ) )
		    ADVANCE( p );
		char * t = p;
		while( !isspace( *p ) )
		    ADVANCE( p );
		ADVANCE( p );
		*(p-1) = '\0';
		if( strcmp( t, "numeric" ) ) {
		    std::cerr << "Warning: treating non-numeric attribute '"
			      << q << "' of type '" << t << "' as numeric\n";
		}
		idx.push_back( q );
	    } else if( !strncasecmp( p, "data", 4 ) ) {
		// From now on everything is data
		int ndim = idx.size();

		do {
		    while( *p != '{' )
			ADVANCE( p );
		    ADVANCE( p );
		    real * coord = new real[ndim](); // zero init
		    do {
			while( isspace( *p ) )
			    ADVANCE( p );
			unsigned long i = strtoul( p, &p, 10 );
			while( isspace( *p ) )
			    ADVANCE( p );
			if( *p == '?' )
			    CROAK( "missing data not supported" );
			real v = 0;
#if REAL_IS_INT
			v = strtoul( p, &p, 10 );
#else
			v = strtod( p, &p );
#endif
			coord[i] = v;
			while( isspace( *p ) )
			    ADVANCE( p );
			if( *p == ',' )
			    ADVANCE( p );
		    } while( *p != '}' );
		    points.push_back( point( coord, -1 ) );
		} while( 1 );
	    }
	} while( 1 );

    END_OF_FILE:
	int ndim = idx.size();
	minval = new real[ndim];
	maxval = new real[ndim];
	for( int i=0; i < ndim; ++i ) {
	    minval[i] = std::numeric_limits<real>::max();
	    maxval[i] = std::numeric_limits<real>::min();
	}
	cilk_for( int i=0; i < ndim; ++i ) {
	    for( int j=0; j < points.size(); ++j ) {
		real v = points[j].d[i];
		if( minval[i] > v )
		    minval[i] = v;
		if( maxval[i] < v )
		    maxval[i] = v;
	    }
	    for( int j=0; j < points.size(); ++j ) {
		points[j].d[i] = (points[j].d[i] - minval[i])
		    / (maxval[i] - minval[i]+1);
	    }
	}

	std::cerr << "@relation: " << relation << "\n";
	std::cerr << "@attributes: " << idx.size() << "\n";
	std::cerr << "@points: " << points.size() << "\n";
    }
};

int main(int argc, char **argv)
{
    struct timespec begin, end;

    srand( time(NULL) );

    get_time( begin );

    //read args
    parse_args(argc,argv);

    std::cerr << "Available threads: " << __cilkrts_get_nworkers() << "\n";

#if SEQUENTIAL && PMC
    LIKWID_MARKER_INIT;
#endif // SEQUENTIAL && PMC

#if TRACING
    event_tracer::init();
#endif
    
    arff_file arff_data;
    arff_data.read_sparse_file( infile );
    num_dimensions = arff_data.idx.size();
    num_points = arff_data.points.size();
    min_val = arff_data.minval;
    max_val = arff_data.maxval;

    // allocate memory
    // get points
    point * points = &arff_data.points[0];

    // get means
    Centres centres;
    // centres.generate();
    // centres.select( points );

    for( int i=0; i < num_points; ++i ) {
	points[i].cluster = rand() % num_clusters;
	centres.add_point( &points[i] );
    }
    centres.normalize();

    // for(int i = 0; i < num_clusters; i++) {
	// std::cout << "in cluster " << i << " " << centres[i].cluster << " points\n";
    // }

    get_time (end);
    print_time("initialize", begin, end);

    printf("KMeans: Calling MapReduce Scheduler\n");

    // keep re-clustering until means stabilise (no points are reassigned
    // to different clusters)
#if SEQUENTIAL && PMC
    LIKWID_MARKER_START("mapreduce");
#endif // SEQUENTIAL && PMC
    get_time (begin);        
    int niter = 1;
    while(kmeans_cluster(centres, points))
	niter++;
    get_time (end);        
#if SEQUENTIAL && PMC
    LIKWID_MARKER_STOP("mapreduce");
#endif // SEQUENTIAL && PMC

    print_time("library", begin, end);

    get_time (begin);

    //print means
    printf("KMeans: MapReduce Completed\n");  
#if 0
    printf("\n\nFinal means:\n");
    for(int i = 0; i < num_clusters; i++)
	centres[i].dump();
#endif
    fprintf( stdout, "iterations: %d\n", niter );

    real sse = 0;
    for( int i=0; i < num_points; ++i ) {
	sse += centres[points[i].cluster].sq_dist( points[i] );
    }
    fprintf( stdout, "within cluster sum of squared errors: %11.4lf\n", sse );

    fprintf( stdout, "%37s\n", "Cluster#" );
    fprintf( stdout, "%-16s", "Attribute" );
    fprintf( stdout, "%10s", "Full Data" );
    for( int i=0; i < num_clusters; ++i )
	fprintf( stdout, "%11d", i );
    fprintf( stdout, "\n" );

    char buf[32];
    sprintf( buf, "(%d)", num_points );
    fprintf( stdout, "%26s", buf );
    for( int i=0; i < num_clusters; ++i ) {
	sprintf( buf, "(%d)", centres[i].cluster );
	fprintf( stdout, "%11s", buf );
    }
    fprintf( stdout, "\n" );

    fprintf( stdout, "================" );
    fprintf( stdout, "==========" );
    for( int i=0; i < num_clusters; ++i )
	fprintf( stdout, "===========" );
    fprintf( stdout, "\n" );

    for( int i=0; i < num_dimensions; ++i ) {
	fprintf( stdout, "%-16s", arff_data.idx[i] );
#if REAL_IS_INT
#error not yet implemented
#else
	real s = 0;
	for( int j=0; j < num_points; ++j )
	    s += points[j].d[i];
	s /= (real)num_points;
	s = min_val[i] + s * (max_val[i] - min_val[i] + 1);
	fprintf( stdout, "%10.4lf", s );
#endif
	for( int k=0; k < num_clusters; ++k ) {
#if REAL_IS_INT
#error not yet implemented
#else
	    real s = 0;
	    for( int j=0; j < num_points; ++j )
		if( points[j].cluster == k )
		    s += points[j].d[i];
	    s /= (real)centres[k].cluster;
	    s = min_val[i] + s * (max_val[i] - min_val[i] + 1);
	    fprintf( stdout, "%11.4lf", s );
#endif
	}
	fprintf( stdout, "\n" );
    }

    //free memory
    // delete[] points; -- done in arff_file
    // oops, not freeing points[i].d 

    get_time (end);
    print_time("finalize", begin, end);

#if TRACING
    event_tracer::destroy();
#endif
    
#if SEQUENTIAL && PMC
    LIKWID_MARKER_CLOSE;
#endif // SEQUENTIAL && PMC
    
    return 0;
}
