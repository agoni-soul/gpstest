import 'package:flutter_study/model/push_event_commit.dart';
import 'package:flutter_study/model/release.dart';
import 'package:json_annotation/json_annotation.dart';

import 'issue.dart';
import 'issue_event.dart';

/**
 * Created by guoshuyu
 * Date: 2018-07-31
 */

part 'event_payload.g.dart';

@JsonSerializable()
class EventPayload {

  @JsonKey(name: "push_id")
  int? pushId;
  int? size;
  @JsonKey(name: "distinct_size")
  int? distinctSize;
  String? ref;
  String? head;
  String? before;
  List<PushEventCommit>? commits;

  String? action;
  @JsonKey(name: "ref_type")
  String? refType;
  @JsonKey(name: "master_branch")
  String? masterBranch;
  String? description;
  @JsonKey(name: "pusher_type")
  String? pusherType;

  Release? release;
  Issue? issue;
  IssueEvent? comment;

  EventPayload();

  factory EventPayload.fromJson(Map<String, dynamic> json) => _$EventPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$EventPayloadToJson(this);
}
