EXTENDS Naturals, Sequences

CONSTANTS
    Dependencies,
    Versions,
    Loaders,
    ExternalResults

VARIABLES
    cache,
    request,
    result

(*
cache:
    Map from lookup key -> dependency list

request:
    Current lookup request

result:
    Returned value from resolveDependency
*)

Key(dep, ver, loader) ==
    <<dep, ver, loader>>

Init ==
    /\ cache = [k \in {} |-> <<>>]
    /\ request = NULL
    /\ result = NULL


RequestLookup(dep, ver, loader) ==
    /\ request = Null
    /\ dep \in Dependencies
    /\ ver \in Versions
    /\ loader \in Loaders
    /\ request' = Key(dep, ver, loader)
    /\ UNCHANGED <<cache, result>>


CacheHit ==
    /\ request # NULL
    /\ request \in DOMAIN cache
    /\ result' = cache[request]
    /\ request' = NULL
    /\ UNCHANGED cache


FetchExternal ==
    /\ request # NULL
    /\ request \notin DOMAIN cache
    /\ \E r \in ExternalResults:
        /\ result' = r
        /\ cache' = [cache EXCEPT ![request] = r]
    /\ request' = NULL


Next ==
    \/ \E d \in Dependencies, v \in Versions, l \in Loaders:
        RequestLookup(d, v, l)
    \/ CacheHit
    \/ FetchExternal


Spec ==
    Init /\ [][Next]_<<cache, request, result>>


(***************************************************************************)
(* Safety Properties                                                       *)
(***************************************************************************)

CacheConsistency ==
    \A k \in DOMAIN cache:
        cache[k] \in ExternalResults

NoNullCache ==
    \A k \in DOMAIN cache:
        cache[k] # NULL
