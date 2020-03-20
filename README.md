# mls-server-scala

* How to run: `>sbt run`
* How to unit test: `>sbt test`
* How to test manually:
```
>sbt run
# and in another terminal:
>http://localhost:10100/groups/[group name]/blobs
# to get the current list of blobs for [group name]
> curl --header "Content-Type: application/json" --request POST --data '{"index":[index],"content":"[json content]"}' http://localhost:10100/groups/[group name]/blobs
#to add a new blob with index and json content to [group name]
# or to create a new group with the name [group name] and the first blob
```
