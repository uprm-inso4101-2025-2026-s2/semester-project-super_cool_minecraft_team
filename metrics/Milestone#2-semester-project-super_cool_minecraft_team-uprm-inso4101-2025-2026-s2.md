# Milestone Data

## Date Generated: 2026-03-07
| Developer | Points Closed | Percent Contribution | Indivudal Grade | Milestone Grade | Lecture Topic Tasks |
| --------- | ------------- | -------------------- | --------------- | --------------- | ------------------- |
| Total | 0.0 | /100% | /100% | /100% | 0 |


## Sprint Task Completion

| Developer | Sprint 1 [current]<br>2026/02/28, 08:00 AM<br>2026/03/17, 02:00 PM | Sprint 2<br>2026/03/17, 02:00 PM<br>2026/04/03, 08:00 PM |
|---|---|---|

## Weekly Discussion Participation

| Developer | Week #1 | Week #2 | Week #3 | Week #4 | Week #5 | Week #6 | Penalty |
|---|---|---|---|---|---|---|---|

## Point Percent by Label

# Metrics Generation Logs

| Message |
| ------- |
| WARNING: list index out of range |
| INFO: Found Project(name='semester-project-super_cool_minecraft_team', number=3, url='https://github.com/orgs/uprm-inso4101-2025-2026-s2/projects/3', public=False) |
| WARNING: Project visibility is set to private. This can lead to issues not being found if the Personal Access Token doesn't have permissions for viewing private projects. |
| WARNING: [Issue #114](https://github.com/uprm-inso4101-2025-2026-s2/semester-project-super_cool_minecraft_team/issues/114) is not associated with a milestone. |
| ERROR: list index out of range |
| Traceback (most recent call last): |
|   File "/home/runner/work/semester-project-super_cool_minecraft_team/semester-project-super_cool_minecraft_team/inso-gh-query-metrics/src/generateMilestoneMetricsForActions.py", line 117, in generateMetricsFromV2Config |
|     discussions=getDiscussions(org=organization, team=team), |
|                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ |
|   File "/home/runner/work/semester-project-super_cool_minecraft_team/semester-project-super_cool_minecraft_team/inso-gh-query-metrics/src/utils/discussions.py", line 184, in getDiscussions |
|     return [ |
|            ^ |
|   File "/home/runner/work/semester-project-super_cool_minecraft_team/semester-project-super_cool_minecraft_team/inso-gh-query-metrics/src/utils/discussions.py", line 184, in <listcomp> |
|     return [ |
|            ^ |
|   File "/home/runner/work/semester-project-super_cool_minecraft_team/semester-project-super_cool_minecraft_team/inso-gh-query-metrics/src/utils/discussions.py", line 146, in getDiscussionDicts |
|     discussion_info: dict = response["organization"]["teams"]["nodes"][0][ |
|                             ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~^^^ |
| IndexError: list index out of range |
