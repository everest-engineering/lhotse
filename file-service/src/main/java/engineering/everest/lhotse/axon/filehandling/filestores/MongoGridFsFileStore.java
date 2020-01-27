package engineering.everest.lhotse.axon.filehandling.filestores;

import engineering.everest.lhotse.axon.filehandling.FileStore;
import engineering.everest.lhotse.axon.filehandling.NativeStorageType;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import java.io.IOException;
import java.io.InputStream;

import static engineering.everest.lhotse.axon.filehandling.NativeStorageType.MONGO_GRID_FS;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class MongoGridFsFileStore implements FileStore {

    private final GridFsTemplate gridFs;

    public MongoGridFsFileStore(GridFsTemplate gridFs) {
        this.gridFs = gridFs;
    }


    @Override
    public String create(InputStream inputStream, String fileName) {
        ObjectId mongoObjectId = gridFs.store(inputStream, fileName);
        return mongoObjectId.toHexString();
    }

    @Override
    public void delete(String fileIdentifier) {
        gridFs.delete(query(where("_id").is(new ObjectId(fileIdentifier))));
    }

    @Override
    public InputStream read(String fileIdentifier) throws IOException {
        var gridFSFile = gridFs.findOne(new Query(where("_id").is(fileIdentifier)));
        if (gridFSFile == null) {
            throw new RuntimeException("Unable to retrieve file " + fileIdentifier);
        }
        return gridFs.getResource(gridFSFile).getInputStream();
    }

    @Override
    public NativeStorageType nativeStorageType() {
        return MONGO_GRID_FS;
    }
}
