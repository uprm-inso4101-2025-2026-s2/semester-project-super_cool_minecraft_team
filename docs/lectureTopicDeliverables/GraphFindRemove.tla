---------------- MODULE GraphFindRemove ----------------

EXTENDS Naturals, Sequences, FiniteSets

VARIABLES 
    nodes,      \* The set of nodes in the graph
    foundNode   \* A variable to represent the result of a find operation

\* Define a sample set of nodes that can be used in operations.
PossibleNodes == {[id |-> "node1"], [id |-> "node2"], [id |-> "node3"]}

\* The initial state of the system. The graph starts with no nodes.
Init == 
    /\ nodes = {}
    /\ foundNode = "null"

\* -- FindNode Operation --
\* Action if the node exists in the set of nodes.
\* The 'foundNode' variable is updated, but the set of nodes is unchanged.
FindNode_Success(key) == 
    /\ key \in nodes
    /\ foundNode' = key
    /\ UNCHANGED nodes

\* Action if the node does not exist.
\* The 'foundNode' is set to a null value, and the set of nodes is unchanged.
FindNode_Fail(key) == 
    /\ key \notin nodes
    /\ foundNode' = "null"
    /\ UNCHANGED nodes

\* The full FindNode operation is either a success or a failure.
FindNode(key) == FindNode_Success(key) \/ FindNode_Fail(key)

\* -- RemoveNode Operation --
\* Action to remove a node if it exists.
\* The set of nodes is updated to exclude the key.
RemoveNode_Success(key) ==
    /\ key \in nodes
    /\ nodes' = nodes \ {key}
    /\ UNCHANGED foundNode

\* Action if the node to be removed does not exist.
\* The set of nodes is unchanged.
RemoveNode_Fail(key) ==
    /\ key \notin nodes
    /\ UNCHANGED <<nodes, foundNode>>

\* The full RemoveNode operation is either a success or a failure.
RemoveNode(key) == RemoveNode_Success(key) \/ RemoveNode_Fail(key)

\* -- Next-State Relation --
\* The next state of the system is the result of either a FindNode or a RemoveNode
\* action being performed with some node from the set of possible nodes.
Next ==
    \E key \in PossibleNodes :
        \/ FindNode(key)
        \/ RemoveNode(key)

=============================================================================