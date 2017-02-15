/**
 * @name             MoreViewController.swift
 * @partof           zucred AG
 * @description
 * @author	 		Vasco Mouta
 * @created			25.10.2015
 *
 * Copyright (c) 2015 zucred AG All rights reserved.
 * This material, including documentation and any related
 * computer programs, is protected by copyright controlled by
 * zucred AG. All rights are reserved. Copying,
 * including reproducing, storing, adapting or translating, any
 * or all of this material requires the prior written consent of
 * zucred AG. This material also contains confidential
 * information which may not be disclosed to others without the
 * prior written consent of zucred AG.
 */

import UIKit
import zucredApple

class MoreViewController: AppBackViewController {
    
    var keys: [String] = ["Account", "Jokes", "Users"]
    var items: [String:[String]] = ["Account" : ["Settings"], "Jokes" : ["MyJokes", "Favorite", "Recommended"], "Users" : ["Following", "Blocked"]]
    var itemsIcon: [String:[String]] = ["Account" : ["IconSettings"], "Jokes" : ["IconMyJokes", "IconFavorites", "IconRecommended"], "Users" : ["IconFollowing", "IconBlocked"]]
    
    var imagePicker = UIImagePickerController()
    
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var addUserImageButton: UIButton!
    @IBOutlet weak var userImage: UIImageView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        userImage.layer.borderWidth = 1
        userImage.layer.masksToBounds = false
        userImage.layer.borderColor = UIColor.black.cgColor
        userImage.layer.cornerRadius = userImage.frame.height/2
        userImage.clipsToBounds = true
        updateImage()
        tableView.reloadData()
    }
    
    // For the moment since AppBackViewController doesn't support Ananlytics
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(true)
        Analytics.sharedInstance.createScreenView(self.className())
        Analytics.sharedInstance.logger.verbose("TrackUIViewController willAppear \(self.className())")
    }
    
    @IBAction func addImage(_ sender: AnyObject){
        if UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.savedPhotosAlbum){
            
            imagePicker.delegate = self
            imagePicker.sourceType = UIImagePickerControllerSourceType.savedPhotosAlbum;
            imagePicker.allowsEditing = false
            
            self.presentViewController(imagePicker, animated: true, completion: nil)
        }
    }
    
    func updateImage() {
        if let image = JokrAppDelegate.sharedInstance.login?.getLoginUserImage() {
            userImage?.image = image
        } else {
            userImage?.image = UIImage(named: "ImageUnknown")
        }
    }
}

// MARK: Table View Data Source

extension MoreViewController: UITableViewDataSource {
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return keys.count
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return items[keys[section]]!.count
    }
    
    func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        return keys[section]
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let keyValue = keys[indexPath.section]
        let cell = tableView.dequeueReusableCell(withIdentifier: "DetailCell", for: indexPath) as! DetailCell
        if let value = items[keyValue] {
            cell.title?.text = value[indexPath.row]
        }
        if let value = itemsIcon[keyValue] {
            cell.icon?.image = UIImage(named:value[indexPath.row])
        }
        return cell
    }
}

extension MoreViewController: UITableViewDelegate {
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let keyValue = keys[indexPath.section]
        if let value = items[keyValue] {
            delegate?.execute(value[indexPath.row])
        }
        tableView.deselectRow(at: indexPath, animated: true)
        
    }
}

// Mark: Image Picker

extension MoreViewController: UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    
    func imagePickerController(_ picker: UIImagePickerController!, didFinishPickingImage image: UIImage!, editingInfo: NSDictionary!){
        self.dismissViewControllerAnimated(true, completion: { () -> Void in

        })
        JokrAppDelegate.sharedInstance.login?.setLoginUserImage(image){ success in
            if success {
                self.updateImage()
            } else {
                self.showMessage("Fail to store Image", type: .Error, options: nil)
            }
        }
    }
}

class DetailCell: UITableViewCell {
    
    @IBOutlet weak var title: UILabel!
    @IBOutlet weak var icon: UIImageView!

}

