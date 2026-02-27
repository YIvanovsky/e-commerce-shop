package com.dailycodework.buynowdotcom.service.chroma;

import com.dailycodework.buynowdotcom.request.EmbeddingsDeleteRequest;
import org.springframework.ai.chroma.vectorstore.ChromaApi;

import java.util.List;

public interface IChromaService {

  void deleteCollection(String collectionName);

  List<ChromaApi.Collection> listCollections();

  ChromaApi.Collection getCollectionById(String collectionName);

  ChromaApi.GetEmbeddingResponse getEmbeddings(String collectionId);

  void deleteEmbeddingsByCollectionId(EmbeddingsDeleteRequest request);
}
