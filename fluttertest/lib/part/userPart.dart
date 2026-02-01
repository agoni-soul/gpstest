// 声明这是 my_app 库的一部分
part of my_app;

class User {
  String name;
  int age;
  String? login;
  String? avatar_url;
  String _privateInfo = '私有信息'; // 私有变量

  User(this.name, this.age);

  void displayInfo() {
    print('用户: $name, 年龄: $age');
  }

  void _privateMethod() {
    print('私有方法: $_privateInfo');
  }
}
