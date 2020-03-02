// Copyright 2018 Duarte Silveira
// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_share_plugin/receive_share_state.dart';
import 'package:flutter_share_plugin/share.dart';
import 'package:path_provider/path_provider.dart';

void main() {
  runApp(DemoApp());
}

class DemoApp extends StatefulWidget {
  @override
  DemoAppState createState() => DemoAppState();
}

class DemoAppState extends ReceiveShareState<DemoApp> {
  String _text = '';
  String _shared = '';

  @override
  void receiveShare(Share shared) {
    debugPrint("Share received - $shared");
    setState(() {
      _shared = shared.toString();
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Share Plugin Demo',
      home: Scaffold(
          appBar: AppBar(
            title: const Text('Share Plugin Demo'),
          ),
          body: Padding(
            padding: const EdgeInsets.all(24.0),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: <Widget>[
                TextField(
                  decoration: const InputDecoration(
                    labelText: 'Share:',
                    hintText: 'Enter some text and/or link to share',
                  ),
                  maxLines: 2,
                  onChanged: (String value) => setState(() {
                    _text = value;
                  }),
                ),
                const Padding(padding: EdgeInsets.only(top: 24.0)),
                Builder(
                  builder: (BuildContext context) {
                    return Column(
                      children: <Widget>[
                        RaisedButton(
                          child: Text("Share File"),
                          onPressed: () async {
                            final tempDir = await getTemporaryDirectory();
                            final file = File(tempDir.path + "/temp.txt");
                            file.writeAsStringSync("dasdsa");
                            await Share.file(
                                    path: file.path,
                                    text: "Text",
                                    title: "Subject")
                                .share();
                          },
                        ),
                        RaisedButton(
                          child: Text("Share Image"),
                          onPressed: () {},
                        ),
                        RaisedButton(
                          child: Text('Share Text'),
                          onPressed: _text.isEmpty
                              ? null
                              : () {
                            // A builder is used to retrieve the context immediately
                            // surrounding the RaisedButton.
                            //
                            // The context's `findRenderObject` returns the first
                            // RenderObject in its descendent tree when it's not
                            // a RenderObjectWidget. The RaisedButton's RenderObject
                            // has its position and size after it's built.
                            final RenderBox box =
                            context.findRenderObject();
                            Share.plainText(text: _text).share(
                                sharePositionOrigin:
                                box.localToGlobal(Offset.zero) &
                                box.size);
//                              Share.image(path: "content://0@media/external/images/media/2129", mimeType: ShareType.TYPE_IMAGE).share(
//                                  sharePositionOrigin:
//                                      box.localToGlobal(Offset.zero) &
//                                          box.size);
                          },
                        ),
                      ],
                    );
                  },
                ),
                const Padding(padding: EdgeInsets.only(top: 24.0)),
                RaisedButton(
                  child: const Text('Toggle share receiving'),
                  onPressed: () {
                    if (!shareReceiveEnabled) {
                      enableShareReceiving();
                    } else {
                      disableShareReceiving();
                    }
                  },
                ),
                const Padding(padding: EdgeInsets.only(top: 24.0)),
                Text(_shared),
              ],
            ),
          )),
    );
  }
}
