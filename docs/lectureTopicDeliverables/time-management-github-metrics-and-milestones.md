# Lecture Topic Task: Time Management Through GitHub Metrics and Milestone Planning

## Selected lecture topic

The lecture topic used here is **Scheduling and Planning**, especially the time management material.

The lecture emphasizes that engineers need actual process data to manage time and improve plans.

It also emphasizes that estimates, actual work, notes, and task categories should be documented.

This connects to the repository because the project contains process artifacts in addition to application code.

The repository tracks milestones, issues, labels, sprints, and generated metrics.

## Project files involved

This relation mainly involves these files and project artifacts:

- `gh_metrics_config.json`
- `.github/workflows/dev-metrics.yml`
- `Proposal.adoc`
- GitHub issues
- GitHub milestones
- task labels and pull requests

The file `gh_metrics_config.json` defines project managers, milestone windows, projected group grades, lecture topic task quota, sprint count, and minimum tasks per sprint.

The GitHub Actions workflow runs a metrics generator and commits milestone metrics to a metrics branch.

The proposal describes milestones, evaluation, implementation, and reporting expectations.

Together, these artifacts form the project's planning system.


## Repository evidence

The metrics configuration defines three milestone windows.

Milestone #1 runs from February 4 to February 27.

Milestone #2 runs from March 2 to April 5.

Milestone #3 runs from May 6 to May 27.

The same file sets `lectureTopicTaskQuota` to `4`.

It sets `countOpenIssues` to `true`.

It sets `sprints` to `2`.

It sets `minTasksPerSprint` to `1`.

This means the repository encodes course planning rules in a machine-readable format.

The workflow then uses that configuration to generate project metrics.

## Relation to time management

The lecture's planning idea can be summarized as: plan, record, compare, and improve.

A GitHub issue represents planned work.

A milestone groups work into a schedule period.

A label categorizes the work.

A pull request gives evidence that work was performed.

The metrics workflow helps collect information about project progress.

This makes the repository more than a source code folder.

It becomes a planning instrument.

## Role of labels

Labels support the lecture's idea that tasks should be categorized.

The project uses labels such as `Lecture Topic Task`, `Task`, `documentation`, `approved`, and `pending approval`.

The `Lecture Topic Task` label makes it possible to query all course-related lecture tasks.

The approval labels help separate proposed work from accepted work.

Documentation labels help identify non-code contributions.

This categorization improves visibility for managers and contributors.

## Planning strengths

The project has explicit milestone periods.

The project has machine-readable metric configuration.

The project has an automated metrics workflow.

The project uses GitHub issues as a backlog.

The project uses labels to categorize tasks.

The project uses pull requests and branches as evidence of implementation.

These practices make the team's process easier to audit.

They also help reconstruct what happened during each milestone.

## Planning weakness

The current system tracks tasks and milestones, but it does not clearly track actual hours spent.

A closed issue proves that work was completed.

It does not show whether the estimate was accurate.

It also does not show whether a one-line bug fix and a multi-day integration task cost the same amount of effort.

The lecture's stronger time management model would compare estimated time against actual time.

That comparison would help the team make better plans in future sprints.


## Why this matters

Users care about receiving a working mod dependency visualizer.

Developers care about avoiding last-minute task avalanches.

Managers care about knowing whether the project is on track.

A visible planning system supports all three groups.

It helps decide whether a task should be completed, split, simplified, or moved to a later milestone.

## Conclusion

The project already contains the foundation of a good time management system.

Milestones, labels, issues, and metrics automation help the team plan and evaluate progress.

The Scheduling and Planning lecture explains why these artifacts matter.
