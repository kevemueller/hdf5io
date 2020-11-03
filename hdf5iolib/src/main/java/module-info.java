/**
 * @author keve
 *
 */
module hdf5iolib {
    requires transitive hdf5ioapi;

    uses app.keve.hdf5io.api.HDF5; // for tests

    provides app.keve.hdf5io.api.HDF5 with app.keve.hdf5io.impl.HDF5Implementation;

    requires java.desktop; // temporarily for BeanInfo in entries

    requires org.yaml.snakeyaml;
    requires org.slf4j;

    exports app.keve.hdf5io.fileformat to org.yaml.snakeyaml;
    exports app.keve.hdf5io.fileformat.level0 to org.yaml.snakeyaml;
    exports app.keve.hdf5io.fileformat.level1 to org.yaml.snakeyaml;
    exports app.keve.hdf5io.fileformat.level2 to org.yaml.snakeyaml;
    exports app.keve.hdf5io.fileformat.level2datatype to org.yaml.snakeyaml;
    exports app.keve.hdf5io.fileformat.level2message to org.yaml.snakeyaml;
    exports app.keve.hdf5io.impl to org.yaml.snakeyaml;

}
