# RyuZUPluginChat/CONTRIBUTING.md へようこそ

私たちのプロジェクトに興味を持っていただきありがとうございます！:sparkles:

[Code of Conduct](./CODE_OF_CONDUCT.md)を遵守し、当プロジェクトを快適で安全な状態に保つようお願いします。

ここではIssueやPR作成の手順、レビューに関すること、PRのマージに関する内容を記載しています。

### Issueについて

#### 新しいIssueを作成する

当プログラムに何かしらバグを発見した場合、ぜひIssueを作成して私たちに知らせてください。公にするのが危険な脆弱性等の報告は、Issueを作成せず私たちの[Discordグループ](https://discord.com/invite/azisaba)に参加し、サポートチケットを作成して報告してください。

また、あなたはIssueを用いて単に機能追加をリクエストすることもできます。その案を採用するかの決定は私たちに委ねられていることを理解した上でIssueを作成してください。

#### Issue解決に貢献する

私たちの[Issue一覧](https://github.com/AzisabaNetwork/RyuZUPluginChat/issues)に解決すべきIssueが載せてあります。あなたはこの一覧の中の解決できそうなIssueに携わったり、自分に合うIssueが存在しない場合はコントリビュートを見送ることができます。

### コードを編集する

当プロジェクトは [GitHub flow](https://docs.github.com/en/get-started/quickstart/github-flow) に従って開発されています。そのためブランチの名前は分かりやすい名前であることが求められます。例えば`feat/add-tell-cmd`や`fix/issue-50`等は良い例です。

基本的な流れは以下の通りです。

1. [IntelliJ IDEA](https://www.jetbrains.com/idea/download/)を導入する

2. [Google Java Style](https://google.github.io/styleguide/javaguide.html)を適用する

3. このリポジトリをフォークする

4. 作業ブランチを作成してコードを書き換える

### 編集した内容をコミットする

満足したタイミングで編集内容をコミットしましょう。コミットの際は https://gitmoji.dev/ を参考にして、コメントの先頭に絵文字でPrefixを付けましょう。また、編集した内容を簡潔にわかりやすくコメントします。

また、コミットメッセージは日本語が推奨されています。これは現状このプロジェクトに携わっているメンバーが全員日本人であり、英語で会話する利点が無いからです。

変更が終了したら、変更内容を自分で見直すことを忘れないでください。PRのレビューを迅速に行えるようにするためです。

### Pull Request

コードの編集が完了したら、Pull Requestを作成しましょう。
- 既存のIssueに基づいた編集の場合はIssueとPRの紐付け([link PR to issue](https://docs.github.com/en/issues/tracking-your-work-with-issues/linking-a-pull-request-to-an-issue))を忘れずに行ってください。
- PRを出す際に[allow maintainer edits](https://docs.github.com/en/github/collaborating-with-issues-and-pull-requests/allowing-changes-to-a-pull-request-branch-created-from-a-fork)にチェックを入れてください。
- コミットメッセージと同様の理由で、PRのタイトルやコメントは日本語が推奨されています。
- PRが提出されると、私たちは編集されたコードについて質問を行う場合があります。
- 同様に、私たちは編集されたコードについて修正を依頼する場合があります。あなたはWebUIから変更を適用することもできますし、あなたのフォークの内容を変更しPushすることで反映することもできます。
- もし何かしらマージについて問題を抱えている場合は、この[git tutorial](https://lab.github.com/githubtraining/managing-merge-conflicts)を参考にしてみてください。

### あなたのPRはマージされました！

おめでとうございます！:tada: そしてありがとうございます！:sparkles:. 

あなたのPRがマージされると、あなたの貢献は[Contributors](https://github.com/AzisabaNetwork/RyuZUPluginChat/graphs/contributors)に公開されます。そして私たちからの感謝の気持ちを受け取ります :sparkles:
