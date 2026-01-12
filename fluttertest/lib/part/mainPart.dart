// 声明这是一个库，库名为 'my_app'
library my_app;

// 引入 product.dart 作为这个库的一部分
part 'productPart.dart';
// 引入 user.dart 作为这个库的一部分
part 'userPart.dart';

void main() {
  // 可以在 main.dart 中使用所有 part 文件中定义的内容
  var user = User('张三', 25);
  user.displayInfo();

  var product = Product('手机', 2999.99);
  product.showDetails();

  // 可以访问 part 文件中的私有变量（下划线开头）
  user._privateMethod(); // 允许访问，因为它们在同一个库中
}
