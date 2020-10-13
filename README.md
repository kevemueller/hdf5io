# hdf5io
Pure Java library to read/write/alter [HDF5](https://www.hdfgroup.org/solutions/hdf5/) (Hierarchical Data Format Version 5) files.

The purpose of this library is to provide Pure Java access to the HDF5 files. It is divided into functional modules dealing with the fileformat,
its interpretation and an easy to use API. Further support is given by a module for integration testing.
The library does not link against the HDF5 C implementation and does not use any code from the C implementation or its high-level language bindings. 
The implementation is purely based on the published [HDF5 File format specification](https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html) with additional bit-level investigation into existing HDF5 files where the specification is incomplete or unclear.

## Motivation
The HDF5 file format is a sophisticated file format to store scientific data in a structured way. This library was created to provide a clean "from the books" implementation of the HDF5 file format specification in order to release the requirement against building native code and using cumbersome JNI wrappers for Java users of the library. It also provides an insight into the complexity of such an implementation as well as feedback towards the HDF Group where their specification is lacking detail for an independent implementation.

## Modules
### hdf5iolib
This module is in charge to provide Java access to the different entities that can be stored/found in HDF5 files. This was the prime focus of development so far. It provides a definition of the entities, including read access (~90% of the entities covered) and write access (~10% of the entities covered) to the underlying data.

### hdf5ioapi
This API exposes a high-level functional view of the underlying HDF5 datafile. It is agnostic to the different versions and methods available with HDF5 to achieve the same things.

### hdf5ioit
This is a small integration test suite to test the API with the implementation provided by hdf5iolib.


## Related work
### HDF Group
The [HDF Group](https://www.hdfgroup.org) is the primary author of HDF5 and the owner of the HDF5 specification as well as the creator of the reference implementation of HDF5 in C language (https://www.hdfgroup.org/downloads/hdf5).
The reference implementation does have amongst other a binding to Java using JNI wrappers [HDF-Java](https://portal.hdfgroup.org/display/support/HDF-Java).

### JHDF5
The [CISD Center for Information Sciences and Databases at the ETH Zurich](https://wiki-bsse.ethz.ch/display/CISD) has created a nice Java minded API on top of the HDF-Java JNI wrapper called [JHDF5 (HDF5 for Java)](https://wiki-bsse.ethz.ch/display/JHDF5).

### James Mudd - jHDF - Pure Java HDF5 library
[James Mudd](https://github.com/jamesmudd) has created (jHDF)[https://github.com/jamesmudd/jhdf] a library to read HDF5 files in pure Java.

