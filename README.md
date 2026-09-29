<p align="center">
  <img width="722" height="352" alt="System architecture diagram" src="https://github.com/user-attachments/assets/efe64e01-14c7-4c25-98e7-b9b764079216" />
</p>
# Project Overview

This project presents an end-to-end GraphRAG (Graph-Enhanced Retrieval-Augmented Generation) architecture that brings relational data structures together with semantic inference processes.

The system consists of two main stages:

1. **Relational Data to Graph Transformation (ETL):** Parses the tables, entities, and foreign key dependencies in a relational database (RDBMS) and models them as nodes and edges on a graph database. This process removes the complexity introduced by multi-layered relational queries while preserving the semantic links between data.
2. **Graph-Powered RAG and LLM Integration:** Thanks to the LLM integration running on top of the graph database, the system goes beyond classical vector-based approaches. While generating a response to a user query, the model traverses the relevant subgraphs to obtain structural and topological context; this minimizes the risk of hallucination and produces outputs with a high level of context awareness.



## Database and Schema Structure

The **Chinook Database** was used as the data source within the scope of this project. Chinook has a comprehensive relational data model covering the artists, albums, tracks, playlists, invoices, customers, and employee hierarchies of a digital media store.

Reasons for choosing this dataset:

* **Rich Relational Network:** The schema contains 11 interrelated tables and numerous foreign key dependencies. The presence of directly or indirectly related tables provides an ideal ground for testing the advantages of graph modeling.
* **Multi-hop Relationship Chains:** The core strength of the GraphRAG architecture is its ability to reason over complex, non-linear relationships. For example, deep chains such as *"the production genres of the albums that the tracks purchased by a customer belong to, and the manager to whom the support employee responsible for that sale reports"* require costly multiple `JOIN` operations in relational databases, whereas in the graph model they are resolved at low cost through direct edge traversals.
* **Standardization and Verifiability:** Being a widely accepted reference schema in the industry makes it easy to verify both the inferences produced by the model and the resulting graph structure.
<p
<img width="836" height="566" alt="chinook" src="https://github.com/user-attachments/assets/b8b6a25e-e568-4b2e-a3ec-20961ce9d9e1" />
/p>


## from Relational to Graph

Before moving on to the stage of transforming relational data into the graph model, it is necessary to understand the theoretical foundations of the graph data model and the architectural advantages it offers compared to traditional relational systems.

### What Is a Graph?
A graph is a non-linear data structure consisting of **nodes / vertices** that represent entities and **edges / relationships** that express the semantic and structural connections between these entities. Unlike hierarchical or strictly-schematized tables, graph models allow complex, multi-directional, and cyclic network relationships to be stored in their natural form. 
For more information: https://www.geeksforgeeks.org/dsa/introduction-to-graphs-data-structure-and-algorithm-tutorials/

### Why a Graph Database (Graph DB)?
In traditional relational databases (RDBMS), querying deep relationships requires costly `JOIN` tables and Cartesian products. As the relationship depth (number of hops) increases, query execution time grows exponentially. 
For more information: https://neo4j.com/docs/getting-started/graph-database/

Graph databases work with an **Index-Free Adjacency** architecture. Each node stores the direct memory addresses (pointers) of the neighboring nodes it is connected to. As a result, regardless of the total size of the database, the cost of traversing between relationships is constant, taking $O(1)$ time.

| Criterion | Relational Database (RDBMS) | Graph Database (Graph DB) |
| :--- | :--- | :--- |
| **Core Structure** | Tables, Rows, Columns | Nodes, Edges, Properties |
| **Relationship Management** | Foreign Keys (FK) and `JOIN` tables | Direct physical connections (Edges / Pointers) |
| **Deep Relationship Cost**| High latency in multiple `JOIN` operations | Depth-independent, constant-cost traversal ($O(1)$ Traversal) |
| **Schema Flexibility** | Rigid, predefined schema (Strict Schema) | Flexible, dynamic, and semi-structured schema |
| **Optimization Area** | Bulk analytical operations and single-record reads | Network analysis, ontologies, and multi-hop queries |

#### Why Neo4j?
Among the graph databases available on the market (Neo4j, Amazon Neptune, Memgraph, ArangoDB, etc.), **Neo4j** was chosen:
* **Native Graph Architecture (Native Graph Storage):** Stores data directly as a graph at both the storage and processing layers.
* **Cypher Query Language:** A rich declarative syntax that makes it possible to express graph traversals intuitively and in an optimized way compared to SQL.
* **Integration and Ecosystem:** Vector indexing capabilities, LLM/GraphRAG libraries, and rich driver support.
For more information: https://neo4j.com/docs/getting-started/whats-neo4j/

---

### Database Support and Transformation (ETL) Process

The system architecture is designed in a modular structure that supports data extraction from 3 different relational databases: **MySQL**, **Microsoft SQL Server (MSSQL)**, and **PostgreSQL**. **MySQL** was used as the reference source during implementation and testing.

#### Transformation Methodology (Two-Phase Ingestion)
Instead of standard CSV/TSV export tools, a two-phase ETL approach that preserves structural consistency and relational integrity was applied to transfer RDBMS data to the graph database:

1. **Phase 1 - Node Extraction and Generation (Node Ingestion):**
   * Main entity records with a Primary Key in the relational tables are scanned.
   * Each table row is imported into Neo4j as an independent node with its corresponding entity label (e.g., `:Customer`, `:Track`, `:Album`) and its properties.

2. **Phase 2 - Edge Mapping and Construction (Edge Resolution):**
   * Foreign Key dependencies between tables are analyzed.
   * Previously created source and target nodes are matched via their unique identifiers.
   * By creating meaningful, directed edges between entities (e.g., `(:ARTIST)-[:HAS_ALBUM]->(:ALBUM)` or `(:ALBUM)-[:HAS_TRACK]->(:TRACK)`), the relational data is transformed into a topological graph network.

Thanks to this method, the RDBMS schema has been transformed into a rich Knowledge Graph without any loss of relational integrity.




## GraphRAG Methods and Inference Architecture

There are different retrieval paradigms for applying Retrieval-Augmented Generation (RAG) on graph databases. Within the scope of this project, two main architectures were evaluated by taking the structure of the Chinook dataset into account, and the method best suited to the purpose was then integrated into the production pipeline.

---

### Method 1: Graph-Enhanced Vector Search (Relationship-Level Vectorization and Subgraph Expansion)

In this approach, the graph structure is carried directly into the semantic similarity space:

1. **Triple Verbalization:** Triples in the graph are converted into natural language sentences. For example, considering an `(:Artist)-[:HAS_ALBUM]->(:Album)` link, a `sentence` property is assigned to the edge:
   > *"AC/DC has album For Those About to Rock We Salute You"*
2. **Vectorization and Indexing:** These generated semantic sentences are transferred to the vector space through a text embedding model, and a vector index is created in the graph database.
3. **Semantic Search and Subgraph Extraction:** The user query is vectorized with the same model, and the $k$ closest edges (e.g., the top 2 relationships) are determined via cosine similarity. Starting from the source and target nodes of these edges, $2$- or $3$-hop neighborhoods are scanned and the relevant subgraph is isolated.
4. **LLM Generation:** The extracted subgraph is fed to the LLM as textual/structured context and a response is generated.

> **Evaluation:** Although it is an effective method for semantic similarity, the cost of generating text and vectors for every relationship in the database is high. It also carries the risk of retrieving incomplete context in aggregate calculations (e.g., *"Who are the top 5 best-selling artists?"*).

---

### Method 2: Text-to-Cypher (Preferred Method)

The **Text-to-Cypher** method was chosen as the primary retrieval mechanism in the project.

#### What Is Cypher?
Cypher is a declarative query language developed by Neo4j that works on graphs with pattern matching logic. Instead of the complex `JOIN` structures in SQL, it uses an intuitive, ASCII-art-like syntax that expresses relationships visually (e.g., `MATCH (a:Artist)-[:HAS_ALBUM]->(b:Album) RETURN a, b`).

#### How It Works

1. **Schema Injection (Schema Prompting):** The ontology of the graph data model is provided to the LLM:
   * Node Labels in the graph
   * Properties that the nodes have
   * Valid relationship types connecting the nodes to each other (Relationship Types & Directions)
2. **Cypher Query Generation:** The question the user submits in natural language is converted by the LLM into the target Cypher query in accordance with the provided schema constraints.
3. **Graph Execution Layer:** The generated Cypher query is executed directly on the Neo4j database engine and a deterministic result set is obtained.
4. **Contextual Response Generation:** The exact data returned from the database is passed to the LLM together with the original question as the final prompt, and a verified response is presented to the user in natural language.

---

### Why Was Text-to-Cypher Chosen?

| Criterion | Graph-Enhanced Vector Search | Text-to-Cypher (Selected) |
| :--- | :--- | :--- |
| **Query Type Compatibility** | Similarity and semantic matching | Aggregation (`COUNT`, `SUM`), filtering, and precise analytics |
| **Storage and Computation Cost**| High (Embedding generation and indexing for all edges) | Low (Only schema definition and query generation) |
| **Hallucination Risk** | Risk of irrelevant subgraphs being included in the context | Dataset verified directly by the database engine |
| **Data Freshness** | Embeddings must be updated as the graph changes | The most up-to-date data is queried directly the moment the graph is updated |

You can find GraphRAG methods right here: https://graphrag.com/concepts/intro-to-graphrag/

### Prerequisites
* **Java:** JDK 17 or higher (depending on the version used)
* **Build Tool:** Maven or Gradle
* **Relational Database:** MySQL (or MSSQL / PostgreSQL)
* **Graph Database:** Neo4j (Local Instance or Neo4j Desktop)
* **LLM Access:** Google Gemini API Key




