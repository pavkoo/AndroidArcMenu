# AndroidArcMenu
android arc menu with a float button ,this view is made for the purpose of fitting big screen.

#Demo
![android arc menu](https://github.com/pavkoo/AndroidArcMenu/blob/master/documents/screen.png)
![android arc menu](https://github.com/pavkoo/AndroidArcMenu/blob/master/documents/demo.gif)

#Usage
in order to use this view, there are 3 steps:
1. set the parent view of the menu
    menu.setParentBlurView(rlParent);
    
2. create a item click listener to handle the user click
   GFloatingMenu.OnItemClickListener mListener;
3. add menu item 
  menu.AddMenuItem((bitmap)icon, "some menu item", mListener);
  
  
#Library licenses
Copyright 2016 PAVKOO

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.



