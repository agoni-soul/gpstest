import 'package:json_annotation/json_annotation.dart';

import 'package:flutter_study/model/user.dart';
import 'commitFile.dart';
import 'commit_git_info.dart';
import 'commit_stats.dart';
import 'repo_commit.dart';

/**
 * Created by guoshuyu
 * Date: 2018-07-31
 */

part 'push_commit.g.dart';

@JsonSerializable()
class PushCommit {
  List<CommitFile>? files;

  CommitStats? stats;

  String? sha;
  String? url;
  @JsonKey(name: "html_url")
  String? htmlUrl;
  @JsonKey(name: "comments_url")
  String? commentsUrl;

  CommitGitInfo? commit;
  User? author;
  User? committer;
  List<RepoCommit>? parents;

  PushCommit(
    this.files,
    this.stats,
    this.sha,
    this.url,
    this.htmlUrl,
    this.commentsUrl,
    this.commit,
    this.author,
    this.committer,
    this.parents,
  );

  factory PushCommit.fromJson(Map<String, dynamic> json) => _$PushCommitFromJson(json);

  Map<String, dynamic> toJson() => _$PushCommitToJson(this);
}
