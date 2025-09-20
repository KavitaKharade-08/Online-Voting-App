from flask import Flask, request, jsonify
import os, cv2, numpy as np, base64
from deepface import DeepFace
from flask_cors import CORS
from PIL import Image, ImageOps
import io

app = Flask(__name__)
CORS(app)

STORED_IMAGES_DIR = "Stored_faces"
UPLOADS_DIR = "uploads"

# Ensure necessary folders exist
os.makedirs(STORED_IMAGES_DIR, exist_ok=True)
os.makedirs(UPLOADS_DIR, exist_ok=True)

@app.route("/")
def home():
    return "Face verification server is running"


@app.route('/match_face', methods=['POST'])
def match_face():
    data = request.form
    voter_id = data.get('voter_id')
    image_data = data.get('image')

    print("📩 Received Voter ID:", voter_id)
    print("📸 Image length:", len(image_data) if image_data else "No image received")

    try:
        # Decode the image and auto-rotate
        image_bytes = base64.b64decode(image_data)
        image = Image.open(io.BytesIO(image_bytes))

        # Convert and auto-rotate using EXIF
        image = image.convert("RGB")
        image = ImageOps.exif_transpose(image)

        # ✅ Rotate 90 degrees left for current image only
        image = image.rotate(90, expand=True)


        # Extra check: if image is upside down, rotate 180
        if image.width < image.height and np.array(image)[0][0][0] < 30:  # crude brightness-based check at top
            print("🔄 Detected likely upside-down image — rotating 180 degrees")
            image = image.rotate(180, expand=True)

        # Save corrected image
        current_image_path = os.path.join(UPLOADS_DIR, "current.jpg")
        image.save(current_image_path, format='JPEG')

        print(f"✅ Image saved and orientation corrected at {current_image_path}")

        # Path to saved voter face image
        stored_image_path = os.path.join(STORED_IMAGES_DIR, f"{voter_id}.jpg")

        if not os.path.exists(stored_image_path):
            print("❌ Stored image not found!")
            return jsonify({"verified": False, "error": "Stored image not found"})

        # Perform face verification
        result = DeepFace.verify(
            img1_path=current_image_path,
            img2_path=stored_image_path,
            enforce_detection=False,
            model_name='Facenet',
            distance_metric='cosine',
            threshold=0.6
        )

        print("🔍 Verification Result:", result)

        if result["verified"]:
            return jsonify({"verified": True})
        else:
            return jsonify({"verified": False})

    except Exception as e:
        print("🚨 Error during face matching:", str(e))
        return jsonify({"verified": False, "error": str(e)})


@app.route('/register_face', methods=['POST'])
def register_face():
    try:
        if 'image' not in request.files or 'voter_id' not in request.form:
            return jsonify({"success": False, "message": "Missing image or voter_id"}), 400

        image_file = request.files['image']
        voter_id = request.form['voter_id']

        save_path = os.path.join(STORED_IMAGES_DIR, f"{voter_id}.jpg")
        image_file.save(save_path)

        return jsonify({"success": True, "message": "Face registered successfully"}), 200

    except Exception as e:
        print("🔥 Register Error:", str(e))
        return jsonify({"success": False, "message": f"Server Error: {str(e)}"}), 500


if __name__ == "__main__":
    # DeepFace test (manual run before starting the server)
    try:
        current_image_path = "uploads/current.jpg"
        stored_image_path = "Stored_faces/ABC1234567.jpg"

        if not os.path.exists(current_image_path):
            print(f"❌ {current_image_path} does not exist!")
        elif not os.path.exists(stored_image_path):
            print(f"❌ {stored_image_path} does not exist!")
        else:
            try:
                with Image.open(current_image_path) as img:
                    img.verify()
                print("✅ Current image is valid")
            except Exception as e:
                print(f"❌ Current image is invalid: {str(e)}")

            try:
                with Image.open(stored_image_path) as img:
                    img.verify()
                print("✅ Stored image is valid")
            except Exception as e:
                print(f"❌ Stored image is invalid: {str(e)}")

            result = DeepFace.verify(
                img1_path=current_image_path,
                img2_path=stored_image_path,
                enforce_detection=False
            )
            print("✅ Manual Test Result:", result)

    except Exception as e:
        print("❌ Manual test failed:", str(e))

    app.run(host='0.0.0.0', port=5000, debug=True)
