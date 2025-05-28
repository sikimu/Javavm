# バイトコード実行時のメモリ操作シーケンス

## 基本的なメモリ操作フロー

```mermaid
sequenceDiagram
    participant VM as 実行エンジン
    participant Frame as スタックフレーム
    participant Stack as オペランドスタック
    participant Locals as ローカル変数
    participant Heap as ヒープ領域

    VM->>Frame: フレーム作成
    Frame->>Stack: スタック初期化
    Frame->>Locals: ローカル変数配列初期化
```

## ロード/ストア命令のメモリ操作

```mermaid
sequenceDiagram
    participant Exec as 実行エンジン
    participant Frame as スタックフレーム
    participant Stack as オペランドスタック
    participant Locals as ローカル変数
    participant Verify as メモリ検証

    Note over Exec,Verify: iload実行時
    Exec->>Verify: ローカル変数インデックス検証
    Verify-->>Exec: 検証結果
    Exec->>Locals: 値取得要求
    Locals-->>Stack: 値をプッシュ
    
    Note over Exec,Verify: istore実行時
    Exec->>Verify: スタック状態検証
    Verify-->>Exec: 検証結果
    Stack->>Locals: 値をポップしてストア
```

## 算術演算命令のメモリ操作

```mermaid
sequenceDiagram
    participant Exec as 実行エンジン
    participant Stack as オペランドスタック
    participant Verify as メモリ検証
    participant Error as エラーハンドラ

    Note over Exec,Error: iadd実行時
    Exec->>Verify: スタック深さ検証
    Verify-->>Exec: 検証結果
    
    alt 検証成功
        Stack->>Stack: 値1をポップ
        Stack->>Stack: 値2をポップ
        Stack->>Stack: 結果をプッシュ
    else スタック不足
        Verify->>Error: StackUnderflowException
    end
```

## オブジェクト生成時のメモリ操作

```mermaid
sequenceDiagram
    participant Exec as 実行エンジン
    participant Heap as ヒープ領域
    participant TLAB as TLAB
    participant GC as ガベージコレクタ
    participant Stack as オペランドスタック

    Exec->>TLAB: メモリ割り当て要求
    
    alt TLABに十分な空き
        TLAB-->>Heap: オブジェクト領域確保
        Heap-->>Stack: オブジェクト参照プッシュ
    else TLAB不足
        TLAB->>GC: 新規TLAB要求
        GC-->>TLAB: 新規TLAB割り当て
        TLAB-->>Heap: オブジェクト領域確保
        Heap-->>Stack: オブジェクト参照プッシュ
    end
```

## フィールドアクセス時のメモリ操作

```mermaid
sequenceDiagram
    participant Exec as 実行エンジン
    participant Stack as オペランドスタック
    participant Heap as ヒープ領域
    participant Barrier as メモリバリア
    participant Error as エラーハンドラ

    Note over Exec,Error: getfield実行時
    Stack->>Exec: オブジェクト参照取得
    Exec->>Barrier: 読み込みバリアチェック
    
    alt Nullチェック成功
        Barrier->>Heap: フィールド値読み込み
        Heap-->>Stack: 値をプッシュ
    else Null参照
        Exec->>Error: NullPointerException
    end
```

## メソッド呼び出し時のメモリ操作

```mermaid
sequenceDiagram
    participant Caller as 呼び出し元フレーム
    participant VM as 実行エンジン
    participant Frame as 新規フレーム
    participant Stack as 新規スタック
    participant Locals as 新規ローカル変数

    Caller->>VM: メソッド呼び出し
    VM->>Frame: フレーム作成
    Frame->>Stack: スタック初期化
    Frame->>Locals: 引数転送
    Note over Frame,Locals: ローカル変数の初期化
```

## エラー発生時のメモリ回復

```mermaid
sequenceDiagram
    participant Exec as 実行エンジン
    participant Frame as 現在のフレーム
    participant Stack as オペランドスタック
    participant Error as エラーハンドラ
    participant GC as ガベージコレクタ

    Exec->>Error: 例外発生
    Error->>Frame: スタックアンワインド
    Frame->>Stack: スタッククリア
    Error->>GC: 不要オブジェクト通知
```

## メモリ安全性の確保メカニズム

### 実行時チェック
1. **スタック操作前の検証**
   - スタックオーバーフロー/アンダーフロー
   - 型の整合性
   - null参照

2. **ヒープアクセス時の検証**
   - オブジェクト境界
   - アクセス権限
   - 世代間参照

### メモリバリア
1. **書き込みバリア**
   - 世代間参照の追跡
   - 参照カウント更新
   - 並行GC用の同期

2. **読み込みバリア**
   - オブジェクト移動の追跡
   - 参照の整合性確保

## 実装上の注意点

### パフォーマンス最適化
1. **メモリアクセスパターン**
   - ホットパスの特定
   - キャッシュ効率の向上
   - メモリ階層の活用

2. **バリアコスト**
   - 必要最小限のチェック
   - インライン展開
   - 条件分岐の最適化

### スレッドセーフティ
1. **同期ポイント**
   - メモリ操作の原子性
   - ロック粒度の最適化
   - デッドロック防止

2. **可視性保証**
   - メモリフェンス
   - volatile意味論
   - happens-before関係

### エラー回復
1. **状態の一貫性**
   - トランザクション的な更新
   - ロールバック機構
   - クリーンアップ処理

2. **リソース解放**
   - 確実な解放
   - 循環参照の処理
   - 一時オブジェクトの管理