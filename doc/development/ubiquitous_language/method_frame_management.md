# メソッド呼び出しとスタックフレーム管理

## メソッド呼び出しのライフサイクル

```mermaid
sequenceDiagram
    participant Caller as 呼び出し元フレーム
    participant VM as 実行エンジン
    participant Method as メソッド領域
    participant Frame as 新規フレーム
    participant Stack as スタック管理
    participant Heap as ヒープ領域

    Note over Caller,Heap: メソッド呼び出し開始
    Caller->>VM: invokevirtual/invokespecial/etc
    VM->>Method: メソッド解決
    
    alt メソッド解決成功
        Method-->>VM: メソッド情報
        VM->>Frame: フレーム作成
        VM->>Stack: 引数転送
        Frame->>Stack: オペランドスタック初期化
        Frame->>Stack: ローカル変数配列初期化
        VM->>Frame: 実行開始
    else メソッド未定義
        Method-->>VM: NoSuchMethodError
        VM->>Caller: 例外通知
    end

    Note over Caller,Heap: メソッド実行終了時
    Frame->>Stack: 戻り値準備
    Frame->>VM: フレーム終了通知
    VM->>Stack: フレーム破棄
    Stack->>Caller: 戻り値転送
```

## スタックフレームの状態遷移

```mermaid
stateDiagram-v2
    [*] --> Created: フレーム作成
    Created --> Initialized: 初期化完了
    Initialized --> Executing: 実行開始
    Executing --> Executing: 命令実行
    Executing --> Exception: 例外発生
    Exception --> Unwinding: スタックアンワインド
    Executing --> Completed: 正常終了
    Completed --> [*]: フレーム破棄
    Unwinding --> [*]: フレーム破棄
```

## 例外処理とスタックトレース

```mermaid
sequenceDiagram
    participant Frame as 現在のフレーム
    participant VM as 実行エンジン
    participant Error as 例外ハンドラ
    participant Trace as スタックトレース
    participant Stack as スタック管理

    Note over Frame,Stack: 例外発生時
    Frame->>VM: 例外通知
    VM->>Error: 例外ハンドラ起動
    Error->>Trace: スタックトレース生成開始
    
    loop 各フレーム
        Trace->>Stack: フレーム情報取得
        Stack-->>Trace: メソッド情報
        Trace->>Trace: トレース情報追加
    end
    
    Error->>VM: 例外オブジェクト生成
    VM->>Stack: スタックアンワインド開始
    
    loop アンワインド処理
        Stack->>Frame: フレーム破棄
        alt ハンドラ発見
            Frame-->>VM: 例外ハンドラ情報
            VM->>Frame: ハンドラへジャンプ
        else ハンドラなし
            Frame-->>VM: アンワインド継続
        end
    end
```

## フレーム管理の実装詳細

### フレーム構造
1. **基本情報**
   - 実行メソッド参照
   - プログラムカウンタ
   - フレームタイプ（通常/ネイティブ）

2. **実行環境**
   - オペランドスタック
   - ローカル変数配列
   - 定数プールへの参照

3. **状態管理**
   - 実行状態フラグ
   - 同期状態
   - モニタ情報

### メモリレイアウト
```
+------------------------+
| フレームヘッダ         |
|------------------------|
| 定数プール参照         |
|------------------------|
| メソッド情報           |
|------------------------|
| ローカル変数配列       |
|------------------------|
| オペランドスタック     |
+------------------------+
```

### スレッドセーフティ
1. **フレームの独立性**
   - スレッドローカルなフレームスタック
   - フレーム間の参照制御
   - 同期化されたメソッドの処理

2. **状態の一貫性**
   - アトミックな状態遷移
   - メモリバリアの適用
   - ロック管理

### パフォーマンス最適化
1. **フレーム割り当て**
   - フレームプール
   - スレッドローカルキャッシュ
   - サイズ予測

2. **アクセス最適化**
   - ホットメソッドの特別扱い
   - インライン化の考慮
   - エスケープ解析

## 実装上の注意点

### スタック管理
1. **深さ制限**
   - 最大深さの設定
   - 再帰呼び出しの制御
   - スタックオーバーフロー検出

2. **メモリ効率**
   - フレームサイズの最適化
   - 不要領域の即時解放
   - メモリ断片化の防止

### 例外処理
1. **パフォーマンス考慮**
   - 例外パスの最適化
   - スタックトレース生成の遅延
   - キャッシュの活用

2. **正確性保証**
   - フレーム情報の完全性
   - 例外チェーンの維持
   - リソースの確実な解放

### デバッグサポート
1. **フレーム情報**
   - ソースファイル情報
   - 行番号マッピング
   - ローカル変数名

2. **モニタリング**
   - フレーム使用統計
   - メモリ使用状況
   - パフォーマンス指標