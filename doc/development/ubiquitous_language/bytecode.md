# バイトコード命令関連の用語定義

## 命令の基本構造
- **オペコード（Opcode）**: 1バイトの命令コード
- **オペランド（Operand）**: 命令の追加パラメータ（可変長）
- **スタック効果表記**:
  - `...`: 変更されないスタック部分
  - `→`: 実行前から実行後への変化
  - `[empty]`: 空のスタック
  - 例: `..., value1, value2 → ..., result`

## 命令カテゴリー

### ロード・ストア命令
- **iload**: ローカル変数からint値をロード
  - **形式**: `iload <index>` または `iload_<n>`（n=0-3）
  - **スタック効果**: `... → ..., value`
  - **エラー**: `LocalVariableIndexOutOfBoundsException`

- **istore**: ローカル変数にint値をストア
  - **形式**: `istore <index>` または `istore_<n>`（n=0-3）
  - **スタック効果**: `..., value → ...`
  - **エラー**: `LocalVariableIndexOutOfBoundsException`

### 定数ロード命令
- **iconst**: int定数をスタックにプッシュ
  - **形式**: `iconst_<n>`（n=-1-5）
  - **スタック効果**: `... → ..., <n>`

- **bipush**: byte値をint値としてプッシュ
  - **形式**: `bipush <byte>`
  - **スタック効果**: `... → ..., value`

- **sipush**: short値をint値としてプッシュ
  - **形式**: `sipush <byte1> <byte2>`
  - **スタック効果**: `... → ..., value`

### 算術命令
- **iadd**: int加算
  - **形式**: `iadd`
  - **スタック効果**: `..., value1, value2 → ..., result`
  - **例外**: `StackUnderflowException`（スタック不足時）

- **isub**: int減算
  - **形式**: `isub`
  - **スタック効果**: `..., value1, value2 → ..., result`
  - **例外**: `StackUnderflowException`（スタック不足時）

- **imul**: int乗算
  - **形式**: `imul`
  - **スタック効果**: `..., value1, value2 → ..., result`
  - **例外**: `StackUnderflowException`（スタック不足時）

- **idiv**: int除算
  - **形式**: `idiv`
  - **スタック効果**: `..., value1, value2 → ..., result`
  - **例外**: 
    - `ArithmeticException`（ゼロ除算時）
    - `StackUnderflowException`（スタック不足時）

### 制御命令
- **return**: void戻り値のメソッドから戻る
  - **形式**: `return`
  - **スタック効果**: なし
  - **制約**: voidメソッドでのみ使用可能

- **ireturn**: int値を戻り値としてメソッドから戻る
  - **形式**: `ireturn`
  - **スタック効果**: `..., value → [empty]`
  - **例外**: `StackUnderflowException`（スタック不足時）

### スタック操作命令
- **pop**: スタックのトップ要素を破棄
  - **形式**: `pop`
  - **スタック効果**: `..., value → ...`
  - **例外**: `StackUnderflowException`（スタック不足時）

- **dup**: スタックのトップ要素を複製
  - **形式**: `dup`
  - **スタック効果**: `..., value → ..., value, value`
  - **例外**: `StackUnderflowException`（スタック不足時）

## 命令実行サイクル
1. **フェッチ（Fetch）**: 命令のオペコードを読み取る
2. **デコード（Decode）**: 命令の種類を判別
3. **オペランド取得（Operand Fetch）**: 必要なオペランドを取得
4. **実行（Execute）**: 命令を実行
5. **後処理（Write Back）**: 結果をスタックまたはローカル変数に書き込み

## 実装規約

### 命令クラス構造
- **命令クラス命名**: `<命令名>Instruction`（例: `IloadInstruction`）
- **基本インターフェース**: `Instruction`
- **実行メソッド**: `execute(Frame frame)`
- **フェッチメソッド**: `fetch(ByteCodeStream stream)`

### エラー処理
- **実行時エラー**: 
  - `StackUnderflowException`
  - `StackOverflowException`
  - `LocalVariableIndexOutOfBoundsException`
  - `ArithmeticException`
- **エラーメッセージ形式**: `"<命令名>: <エラー内容>"`

### コメント規約
- **命令説明**: 
```java
/**
 * <命令名>: <命令の説明>
 * フォーマット: <バイトコードフォーマット>
 * スタック: <スタック効果>
 * 例外: <発生する可能性のある例外>
 */
```

## テスト要件
- **基本テスト**: 正常系の動作確認
- **エラーテスト**: 各例外発生条件の確認
- **境界値テスト**: スタックの限界値での動作確認
- **パフォーマンステスト**: 実行速度の要件確認