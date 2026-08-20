// 声明这是 my_app 库的一部分
part of my_app;

class Product {
  String name;
  double price;

  Product(this.name, this.price);

  void showDetails() {
    print('产品: $name, 价格: \$$price');
  }
}
