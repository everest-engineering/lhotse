Some potential tasks for training....

## Remove deleted photos from entered competitions (easy)

Photos are deleted when their owning user is deleted and forgotten. Deleted photos should NOT be removed from the
competitions in which they were entered as this could change perceptions on how competitions panned out. Instead,
make changes to the projections so that the deleted photos are clearly identified instead of relying on 404 responses.
Include the time at which they were removed in the API response body.

## Remove votes (easy)

Create an API that allows a vote to be removed and implement the flow through the domain on to projections.

Consider renaming events.

## Constrain votes within a competition (easy)

There is no limit on the number of photos a registered user can vote for within a competition. Track the votes received,
rejecting when a user has reached their voting quota. Prevent someone from voting for their own work.

## Introduce (optional) moderator approval of submissions (moderate)

Not all holiday snaps are equal and deserving of attention.

Well trusted (verified?) photographers may have their work automatically approved. The rest of us need to wait for a
human.

## Major remodel (moderate++)

We're pivoting the company! 

Assume that the changes are being made to a system already in production. Write event upcasters and perform a replay
(assume that you can disconnect a load balancer so life is easier).
