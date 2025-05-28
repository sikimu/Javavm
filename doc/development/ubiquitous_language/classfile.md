# クラスファイル関連の用語定義

## ファイルフォーマット
- **クラスファイル（Class File）**: Javaバイトコードを含むバイナリファイル。`.class`拡張子を持つ。
- **マジックナンバー（Magic Number）**: クラスファイルの先頭4バイト（`0xCAFEBABE`）。ファイルの識別に使用。
- **バージョン情報（Version Information）**: クラスファイルのバージョンを示す2つの数値。
  - **マイナーバージョン（Minor Version）**: 下位互換性のための番号
  - **メジャーバージョン（Major Version）**: 主要な互換性変更を示す番号

## データ型
- **u1**: 1バイトの符号なし整数
- **u2**: 2バイトの符号なし整数
- **u4**: 4バイトの符号なし整数
- **u8**: 8バイトの符号なし整数

## 定数プール
- **定数プール（Constant Pool）**: クラスファイル内の文字列や型情報などを格納するテーブル
- **定数プールカウント（Constant Pool Count）**: 定数プール内のエントリ数 + 1
- **CONSTANT_Utf8**: UTF-8エンコードされた文字列情報
- **CONSTANT_Class**: クラスまたはインターフェースの参照
- **CONSTANT_NameAndType**: フィールドまたはメソッドの名前と型情報

## アクセス制御
- **アクセスフラグ（Access Flags）**: クラス、メソッド、フィールドのアクセス制御情報
  - **public**: `0x0001`
  - **private**: `0x0002`
  - **protected**: `0x0004`
  - **static**: `0x0008`
  - **final**: `0x0010`

## クラス情報
- **this_class**: 現在のクラスを示すCONSTANT_Class情報へのインデックス
- **super_class**: スーパークラスを示すCONSTANT_Class情報へのインデックス
- **interfaces_count**: インターフェース数
- **interfaces**: 実装するインターフェースの配列

## フィールド情報
- **fields_count**: フィールド数
- **fields**: フィールド情報の配列
- **フィールド属性**: 
  - **ConstantValue**: 定数値
  - **Synthetic**: コンパイラ生成を示す
  - **Deprecated**: 非推奨を示す

## メソッド情報
- **methods_count**: メソッド数
- **methods**: メソッド情報の配列
- **メソッド属性**:
  - **Code**: バイトコード本体
  - **Exceptions**: 例外テーブル
  - **Synthetic**: コンパイラ生成を示す
  - **Deprecated**: 非推奨を示す

## 属性情報
- **attributes_count**: 属性数
- **attributes**: 属性情報の配列
- **標準属性**:
  - **SourceFile**: ソースファイル名
  - **LineNumberTable**: ソースコード行番号との対応
  - **LocalVariableTable**: ローカル変数情報

## 例外関連
- **ClassNotFoundException**: クラスファイルが見つからない場合の例外
- **ClassFormatError**: クラスファイルの形式が不正な場合の例外
- **UnsupportedClassVersionError**: サポートされていないバージョンの場合の例外