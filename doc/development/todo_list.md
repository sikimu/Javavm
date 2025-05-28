# Java VM 実装 TODO リスト

## フェーズ1: クラスローダー基本実装

### 1.1 クラスファイル基本読み込み

#### 1.1.1 ファイルストリーム処理
- [ ] `// TODO: ファイルが存在しない場合にClassNotFoundExceptionをスローする`
  - テスト: `testFileNotFound()`
  - 入力: 存在しないファイルパス
  - 期待値: `ClassNotFoundException`
  - エラーメッセージに具体的なファイルパスが含まれることを確認

- [ ] `// TODO: ファイルが読み取り不可の場合にClassFormatErrorをスローする`
  - テスト: `testFileNotReadable()`
  - 入力: 読み取り権限のないファイル
  - 期待値: `ClassFormatError`
  - テストデータ準備: `Files.setPosixFilePermissions()`を使用

- [ ] `// TODO: ファイルサイズが最大許容サイズを超える場合にClassFormatErrorをスローする`
  - テスト: `testFileTooLarge()`
  - 入力: 100MB以上のファイル
  - 期待値: `ClassFormatError("File size exceeds maximum allowed size")`

- [ ] `// TODO: 空のクラスファイルの場合にClassFormatErrorをスローする`
  - テスト: `testEmptyClassFile()`
  - 入力: 0バイトのファイル
  - 期待値: `ClassFormatError("Empty class file")`

- [ ] `// TODO: ディレクトリが指定された場合にClassNotFoundExceptionをスローする`
  - テスト: `testDirectoryAsClassFile()`
  - 入力: ディレクトリパス
  - 期待値: `ClassNotFoundException`

#### 1.1.2 基本データ型読み取り
- [ ] `// TODO: u1(1バイト)データの読み取り機能を実装する`
  - テスト: `testReadU1()`
  - 入力: `[0xFF]`
  - 期待値: `255`

- [ ] `// TODO: u1の連続読み取り機能を実装する`
  - テスト: `testReadMultipleU1()`
  - 入力: `[0x00, 0x7F, 0xFF]`
  - 期待値: `[0, 127, 255]`

- [ ] `// TODO: u2(2バイト)データの読み取り機能を実装する`
  - テスト: `testReadU2()`
  - 入力: `[0xCA, 0xFE]`
  - 期待値: `51966`

- [ ] `// TODO: u2のバウンダリー値テスト`
  - テスト: `testReadU2Boundaries()`
  - 入力1: `[0x00, 0x00]` (最小値)
  - 期待値1: `0`
  - 入力2: `[0xFF, 0xFF]` (最大値)
  - 期待値2: `65535`

- [ ] `// TODO: u4(4バイト)データの読み取り機能を実装する`
  - テスト: `testReadU4()`
  - 入力: `[0xCA, 0xFE, 0xBA, 0xBE]`
  - 期待値: `3405691582`

- [ ] `// TODO: u4のバウンダリー値テスト`
  - テスト: `testReadU4Boundaries()`
  - 入力1: `[0x00, 0x00, 0x00, 0x00]` (最小値)
  - 期待値1: `0L`
  - 入力2: `[0xFF, 0xFF, 0xFF, 0xFF]` (最大値)
  - 期待値2: `4294967295L`

- [ ] `// TODO: ストリーム終端でのu1読み取り時にClassFormatErrorをスローする`
  - テスト: `testReadU1AtEOF()`
  - 入力: 空のストリーム
  - 期待値: `ClassFormatError("Unexpected end of file")`

- [ ] `// TODO: ストリーム終端でのu2読み取り時にClassFormatErrorをスローする`
  - テスト: `testReadU2AtEOF()`
  - 入力1: 1バイトのストリーム
  - 期待値1: `ClassFormatError("Unexpected end of file while reading u2")`

- [ ] `// TODO: ストリーム終端でのu4読み取り時にClassFormatErrorをスローする`
  - テスト: `testReadU4AtEOF()`
  - 入力1: 3バイトのストリーム
  - 期待値1: `ClassFormatError("Unexpected end of file while reading u4")`

- [ ] `// TODO: 大量データの連続読み取りのパフォーマンステスト`
  - テスト: `testBulkReadPerformance()`
  - 入力: 1MBのランダムデータ
  - 期待値: 1秒以内に処理完了

#### 1.1.3 マジックナンバー検証
- [ ] `// TODO: 正しいマジックナンバーを検証する`
  - テスト: `testValidMagicNumber()`
  - 入力: `[0xCA, 0xFE, 0xBA, 0xBE]`で始まるストリーム
  - 期待値: 検証成功

- [ ] `// TODO: 不正なマジックナンバーの場合にClassFormatErrorをスローする`
  - テスト: `testInvalidMagicNumber()`
  - テストケース1:
    - 入力: `[0x00, 0x00, 0x00, 0x00]`
    - 期待値: `ClassFormatError("Invalid magic number")`
  - テストケース2:
    - 入力: `[0xCA, 0xFE, 0xBA, 0xBF]` (最後の1バイトが異なる)
    - 期待値: `ClassFormatError("Invalid magic number")`
  - テストケース3:
    - 入力: `[0xCA, 0xFE, 0xBA]` (不完全なマジックナンバー)
    - 期待値: `ClassFormatError("Unexpected end of file while reading magic number")`

#### 1.1.4 バージョン情報読み取り
- [ ] `// TODO: minorバージョンの読み取り機能を実装する`
  - テスト: `testReadMinorVersion()`
  - 入力1: マジックナンバー + `[0x00, 0x00]` (minor_version = 0)
  - 期待値1: `0`
  - 入力2: マジックナンバー + `[0x00, 0x03]` (minor_version = 3)
  - 期待値2: `3`

- [ ] `// TODO: majorバージョンの読み取り機能を実装する`
  - テスト: `testReadMajorVersion()`
  - テストケース1:
    - 入力: マジックナンバー + minor_version + `[0x00, 0x37]` (Java 11)
    - 期待値: `55`
  - テストケース2:
    - 入力: マジックナンバー + minor_version + `[0x00, 0x3D]` (Java 17)
    - 期待値: `61`

- [ ] `// TODO: サポートされているすべてのバージョンの検証`
  - テスト: `testSupportedVersions()`
  - テストケース: Java 8から17までの各バージョン
  - 期待値: すべて成功

- [ ] `// TODO: 非対応バージョンの場合にUnsupportedClassVersionErrorをスローする`
  - テスト: `testUnsupportedVersion()`
  - テストケース1:
    - 入力: マジックナンバー + `[0x00, 0x00]` + `[0xFF, 0xFF]` (未来のバージョン)
    - 期待値: `UnsupportedClassVersionError("Unsupported class version: 65535")`
  - テストケース2:
    - 入力: マジックナンバー + `[0x00, 0x00]` + `[0x00, 0x31]` (Java 5以前)
    - 期待値: `UnsupportedClassVersionError("Unsupported class version: 49")`

#### 1.1.5 統合テスト
- [ ] `// TODO: 有効なクラスファイルヘッダーの全体読み取りテスト`
  - テスト: `testValidClassFileHeader()`
  - 入力: 最小の有効なクラスファイル（Hello.class相当）
  - 期待値: すべてのヘッダー情報が正しく読み取られる

- [ ] `// TODO: 実際のクラスファイルを使用した読み取りテスト`
  - テスト: `testRealClassFileReading()`
  - 入力: 
    1. 単純なクラス（空のクラス）
    2. フィールドを含むクラス
    3. メソッドを含むクラス
  - 期待値: それぞれのヘッダーが正しく読み取られる

- [ ] `// TODO: 大きなクラスファイルの読み取りパフォーマンステスト`
  - テスト: `testLargeClassFilePerformance()`
  - 入力: 1MB程度の有効なクラスファイル
  - 期待値: 3秒以内に処理完了

### テストデータの準備

#### テストデータファイル
```
test-resources/
  └── classfiles/
      ├── valid/
      │   ├── Empty.class              # 最小の有効なクラス
      │   ├── HelloWorld.class         # 基本的なメソッドを含むクラス
      │   └── LargeClass.class         # パフォーマンステスト用の大きなクラス
      └── invalid/
          ├── empty.class              # 0バイトファイル
          ├── invalid_magic.class      # 不正なマジックナンバー
          ├── truncated.class          # 不完全なファイル
          └── future_version.class     # 未来のバージョン
```

#### テストユーティリティ
- クラスファイル生成ヘルパー
- バイト配列作成ユーティリティ
- ファイルパーミッション設定ヘルパー
- テストデータクリーンアップユーティリティ

### コードの責務分割

#### ClassFileReader
- バイトストリームからの低レベルデータ読み取り
- ファイル存在確認とオープン
- u1, u2, u4データの読み取り
- 読み取り位置の追跡
- リソース管理（ストリームのクローズ）

#### ClassFileVerifier
- マジックナンバーの検証
- バージョン互換性チェック
- 基本的なフォーマット検証
- エラーメッセージの生成

## テスト方針
1. 各TODOは1つ以上の具体的なテストケースに対応
2. 正常系、異常系、境界値を網羅
3. パフォーマンス要件の検証
4. 実際のクラスファイルを使用したテスト

## 実装ステップ
1. テストデータの準備
2. テストクラスの作成（ClassFileReaderTest, ClassFileVerifierTest）
3. テストユーティリティの実装
4. 各TODOに対応するテストメソッドの実装
5. テストが失敗することを確認（Red）
6. 最小限の実装を行う（Green）
7. リファクタリング（Refactor）
8. 次のTODOに進む

## 品質基準
- テストカバレッジ100%
- すべてのエッジケースのテスト
- パフォーマンス要件の達成
- コードスタイルガイドラインの遵守