# RyuZUPluginChat
## 概要
RedisとLunaChatを使用して、複数マイクラサーバーのグローバルチャット/チャンネルチャット/個人チャットを共有するPlugin

## 機能
* 全体チャットの共有
* LunaChatチャンネルチャットの共有
* サーバー間個人チャット
* 全サーバーへのシステムメッセージの送信
* 独自Prefix/Suffixの指定
* Discordとの連携
* VC読み上げ向けの、メッセージのみDiscordへ転送する機能

## 導入
1. Configを書く
    1. Configを自動生成する場合
        1. [最新リリース](https://github.com/AzisabaNetwork/RyuZUPluginChat/releases/latest)からjarをダウンロードし、pluginsディレクトリに導入する
        2. サーバーを起動して、`plugins/RyuZUPluginChat/config.yml` が生成されたことを確認したらサーバーを閉じる
        3. config.yml の内容を編集する
    2. Configを自分で書く場合
        1. [config.yml](https://github.com/AzisabaNetwork/RyuZUPluginChat/blob/main/src/main/resources/config.yml)の内容を `plugins/RyuZUPluginChat/config.yml` に書く
        2. 内容を任意の内容に編集する
2. サーバーを起動する
3. チャットが共有されていることを確認する

## 設定項目
|  Key  |  Value  |
|:---:|:---:|
|server-name|サーバー名。主に個人チャットの際の表示に使用されます|
|redis.*|Redisサーバーの接続情報|
|redis.group|接続するグループ名。同じグループ名が指定されたサーバーのチャットのみ共有されます|
|formats.*|チャットのフォーマットを指定します。チャンネルチャットはそのチャンネルのフォーマット設定が使用されます|
|discord.*|Discordの接続情報を指定します|

## ライセンス / License
[GNU General Public License v3.0](LICENSE)
